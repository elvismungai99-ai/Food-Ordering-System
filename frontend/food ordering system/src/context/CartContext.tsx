import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  acceptCartPriceChanges,
  addCartItem,
  getCart,
  removeCartItem,
  updateCartItemQuantity,
  type Cart,
} from "../services/CartService";

interface CartContextValue {
  cart: Cart | null;
  totalItems: number;

  loading: boolean;
  error: string;

  refreshCart: () => Promise<void>;

  addToCart: (
    menuItemId: string,
    quantity?: number
  ) => Promise<void>;

  updateQuantity: (
    cartItemId: string,
    quantity: number
  ) => Promise<void>;

  removeItem: (
    cartItemId: string
  ) => Promise<void>;

  acceptPriceChanges:
    () => Promise<void>;

  clearCartState: () => void;
}

interface CartProviderProps {
  children: ReactNode;
}

const CartContext =
  createContext<CartContextValue | undefined>(
    undefined
  );

export function CartProvider({
  children,
}: CartProviderProps) {
  const [cart, setCart] =
    useState<Cart | null>(null);

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState("");

  const clearCartState =
    useCallback(() => {
      setCart(null);
      setError("");
    }, []);

  const refreshCart =
    useCallback(async () => {
      const token =
        localStorage.getItem("token");

      const role =
        localStorage.getItem("role");

      if (
        !token
        || role !== "CUSTOMER"
      ) {
        clearCartState();
        return;
      }

      try {
        setLoading(true);
        setError("");

        const cartData =
          await getCart();

        setCart(cartData);
      } catch (requestError) {
        console.error(
          "Failed to load cart",
          requestError
        );

        setError(
          "Unable to load your cart."
        );
      } finally {
        setLoading(false);
      }
    }, [clearCartState]);

  const addToCart =
    useCallback(
      async (
        menuItemId: string,
        quantity = 1
      ) => {
        try {
          setError("");

          const updatedCart =
            await addCartItem({
              menuItemId,
              quantity,
            });

          setCart(updatedCart);
        } catch (requestError) {
          setError(
            "Unable to add this item to your cart."
          );

          throw requestError;
        }
      },
      []
    );

  const updateQuantity =
    useCallback(
      async (
        cartItemId: string,
        quantity: number
      ) => {
        try {
          setError("");

          const updatedCart =
            await updateCartItemQuantity(
              cartItemId,
              quantity
            );

          setCart(updatedCart);
        } catch (requestError) {
          setError(
            "Unable to update the item quantity."
          );

          throw requestError;
        }
      },
      []
    );

  const removeItem =
    useCallback(
      async (cartItemId: string) => {
        try {
          setError("");

          const updatedCart =
            await removeCartItem(
              cartItemId
            );

          setCart(updatedCart);
        } catch (requestError) {
          setError(
            "Unable to remove the cart item."
          );

          throw requestError;
        }
      },
      []
    );

  const acceptPriceChanges =
    useCallback(async () => {
      try {
        setLoading(true);
        setError("");

        const updatedCart =
          await acceptCartPriceChanges();

        setCart(updatedCart);
      } catch (requestError) {
        setError(
          "Unable to accept the updated prices."
        );

        throw requestError;
      } finally {
        setLoading(false);
      }
    }, []);

  useEffect(() => {
    refreshCart();
  }, [refreshCart]);

  const value = useMemo(
    () => ({
      cart,
      totalItems:
        cart?.totalItems ?? 0,
      loading,
      error,
      refreshCart,
      addToCart,
      updateQuantity,
      removeItem,
      acceptPriceChanges,
      clearCartState,
    }),
    [
      cart,
      loading,
      error,
      refreshCart,
      addToCart,
      updateQuantity,
      removeItem,
      acceptPriceChanges,
      clearCartState,
    ]
  );

  return (
    <CartContext.Provider
      value={value}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart():
CartContextValue {
  const context =
    useContext(CartContext);

  if (!context) {
    throw new Error(
      "useCart must be used inside CartProvider"
    );
  }

  return context;
}