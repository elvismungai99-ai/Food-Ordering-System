import { useEffect, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";

import CustomerHeader from "../../components/customer/CustomerHeader";
import {
  getUserProfile,
  updateUserProfile,
  changeUserPassword,
  getSavedAddresses,
  createSavedAddress,
  deleteSavedAddress,
  setDefaultAddress,
  type UserProfile,
  type SavedAddress,
} from "../../services/UserService";
import { reverseGeocodeLocation } from "../../utils/location";

type ProfileTab = "profile" | "addresses" | "security";

function CustomerProfilePage() {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<ProfileTab>("profile");
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [savedAddresses, setSavedAddresses] = useState<SavedAddress[]>([]);
  const [loading, setLoading] = useState(true);

  const [statusMessage, setStatusMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  // Edit Profile Form state
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [savingProfile, setSavingProfile] = useState(false);

  // Security Form state
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [changingPassword, setChangingPassword] = useState(false);

  // Add Address Modal state
  const [showAddAddressModal, setShowAddAddressModal] = useState(false);
  const [newAddressLabel, setNewAddressLabel] = useState("Home");
  const [newAddressStreet, setNewAddressStreet] = useState("");
  const [newBuildingName, setNewBuildingName] = useState("");
  const [newApartmentNumber, setNewApartmentNumber] = useState("");
  const [newLandmarks, setNewLandmarks] = useState("");
  const [newDeliveryInstructions, setNewDeliveryInstructions] = useState("");
  const [newLatitude, setNewLatitude] = useState<number | undefined>(undefined);
  const [newLongitude, setNewLongitude] = useState<number | undefined>(undefined);
  const [newIsDefault, setNewIsDefault] = useState(false);
  const [locatingAddress, setLocatingAddress] = useState(false);
  const [savingAddress, setSavingAddress] = useState(false);

  const loadData = async () => {
    try {
      setLoading(true);
      setErrorMessage("");

      const [userProfile, addresses] = await Promise.all([
        getUserProfile(),
        getSavedAddresses(),
      ]);

      setProfile(userProfile);
      setFirstName(userProfile.firstName || "");
      setLastName(userProfile.lastName || "");
      setEmail(userProfile.email || "");
      setPhoneNumber(userProfile.phoneNumber || "");

      setSavedAddresses(addresses);
    } catch (err) {
      console.error("Failed to load profile data:", err);
      setErrorMessage("Unable to load profile information.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleUpdateProfile = async (e: FormEvent) => {
    e.preventDefault();
    try {
      setSavingProfile(true);
      setErrorMessage("");
      setStatusMessage("");

      const updated = await updateUserProfile({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim(),
        phoneNumber: phoneNumber.trim(),
      });

      setProfile(updated);
      localStorage.setItem("firstName", updated.firstName);
      setStatusMessage("Profile updated successfully!");
    } catch (err) {
      console.error("Failed to update profile:", err);
      setErrorMessage("Unable to update profile. Please verify your details.");
    } finally {
      setSavingProfile(false);
    }
  };

  const handleChangePassword = async (e: FormEvent) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      setErrorMessage("New password and confirmation do not match.");
      return;
    }
    if (newPassword.length < 6) {
      setErrorMessage("New password must be at least 6 characters long.");
      return;
    }

    try {
      setChangingPassword(true);
      setErrorMessage("");
      setStatusMessage("");

      await changeUserPassword({
        currentPassword,
        newPassword,
      });

      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
      setStatusMessage("Password changed successfully!");
    } catch (err) {
      console.error("Failed to change password:", err);
      setErrorMessage("Incorrect current password or invalid new password.");
    } finally {
      setChangingPassword(false);
    }
  };

  const handleDetectAddressLocation = async () => {
    if (!navigator.geolocation) {
      setErrorMessage("Location not supported by browser.");
      return;
    }

    try {
      setLocatingAddress(true);
      setErrorMessage("");

      const pos = await new Promise<GeolocationPosition>((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject, {
          enableHighAccuracy: true,
          timeout: 12000,
        });
      });

      const lat = Number(pos.coords.latitude.toFixed(6));
      const lon = Number(pos.coords.longitude.toFixed(6));
      setNewLatitude(lat);
      setNewLongitude(lon);

      const resolved = await reverseGeocodeLocation(lat, lon);
      setNewAddressStreet(resolved);
    } catch (err) {
      console.error("GPS detection error:", err);
      setErrorMessage("Unable to detect current GPS location.");
    } finally {
      setLocatingAddress(false);
    }
  };

  const handleCreateAddress = async (e: FormEvent) => {
    e.preventDefault();
    if (!newAddressStreet.trim()) {
      setErrorMessage("Street / Area address is required.");
      return;
    }

    try {
      setSavingAddress(true);
      setErrorMessage("");

      await createSavedAddress({
        label: newAddressLabel.trim(),
        address: newAddressStreet.trim(),
        buildingName: newBuildingName.trim() || undefined,
        apartmentNumber: newApartmentNumber.trim() || undefined,
        landmarks: newLandmarks.trim() || undefined,
        deliveryInstructions: newDeliveryInstructions.trim() || undefined,
        latitude: newLatitude,
        longitude: newLongitude,
        isDefault: newIsDefault,
      });

      setShowAddAddressModal(false);
      resetAddressForm();
      const updatedList = await getSavedAddresses();
      setSavedAddresses(updatedList);
      setStatusMessage("Address saved successfully!");
    } catch (err) {
      console.error("Failed to save address:", err);
      setErrorMessage("Unable to save address.");
    } finally {
      setSavingAddress(false);
    }
  };

  const handleSetDefaultAddress = async (id: string) => {
    try {
      setErrorMessage("");
      await setDefaultAddress(id);
      const updatedList = await getSavedAddresses();
      setSavedAddresses(updatedList);
      setStatusMessage("Default address updated!");
    } catch (err) {
      console.error("Failed to set default address:", err);
      setErrorMessage("Unable to set default address.");
    }
  };

  const handleDeleteAddress = async (id: string) => {
    if (!window.confirm("Are you sure you want to remove this saved address?")) return;
    try {
      setErrorMessage("");
      await deleteSavedAddress(id);
      setSavedAddresses((prev) => prev.filter((a) => a.id !== id));
      setStatusMessage("Address removed.");
    } catch (err) {
      console.error("Failed to delete address:", err);
      setErrorMessage("Unable to delete address.");
    }
  };

  const resetAddressForm = () => {
    setNewAddressLabel("Home");
    setNewAddressStreet("");
    setNewBuildingName("");
    setNewApartmentNumber("");
    setNewLandmarks("");
    setNewDeliveryInstructions("");
    setNewLatitude(undefined);
    setNewLongitude(undefined);
    setNewIsDefault(false);
  };

  const handleLogout = () => {
    localStorage.clear();
    sessionStorage.clear();
    navigate("/login");
  };

  if (loading && !profile) {
    return (
      <div className="min-h-screen bg-slate-100">
        <CustomerHeader />
        <div className="flex h-96 items-center justify-center text-slate-500">
          Loading profile...
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100">
      <CustomerHeader />

      <main className="mx-auto max-w-4xl px-6 py-10">
        <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-slate-950">Customer Profile</h1>
            <p className="mt-1 text-slate-500">
              Manage your personal info, saved delivery addresses, and security.
            </p>
          </div>

          <button
            type="button"
            onClick={handleLogout}
            className="rounded-3xl border border-red-200 bg-red-50 px-5 py-2 text-xs font-bold text-red-700 hover:bg-red-100 transition"
          >
            Log out
          </button>
        </div>

        {statusMessage && (
          <div className="mb-6 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-800 animate-fadeIn">
            ✓ {statusMessage}
          </div>
        )}

        {errorMessage && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {errorMessage}
          </div>
        )}

        {/* Tab Navigation */}
        <div className="flex gap-2 border-b border-slate-200 pb-3 mb-6">
          <button
            type="button"
            onClick={() => {
              setActiveTab("profile");
              setStatusMessage("");
            }}
            className={`rounded-2xl px-5 py-2.5 text-sm font-bold transition ${
              activeTab === "profile"
                ? "bg-indigo-600 text-white shadow-md shadow-indigo-600/20"
                : "bg-white text-slate-700 hover:bg-slate-50 border border-slate-200"
            }`}
          >
            Personal Details
          </button>

          <button
            type="button"
            onClick={() => {
              setActiveTab("addresses");
              setStatusMessage("");
            }}
            className={`rounded-2xl px-5 py-2.5 text-sm font-bold transition ${
              activeTab === "addresses"
                ? "bg-indigo-600 text-white shadow-md shadow-indigo-600/20"
                : "bg-white text-slate-700 hover:bg-slate-50 border border-slate-200"
            }`}
          >
            Saved Addresses ({savedAddresses.length})
          </button>

          <button
            type="button"
            onClick={() => {
              setActiveTab("security");
              setStatusMessage("");
            }}
            className={`rounded-2xl px-5 py-2.5 text-sm font-bold transition ${
              activeTab === "security"
                ? "bg-indigo-600 text-white shadow-md shadow-indigo-600/20"
                : "bg-white text-slate-700 hover:bg-slate-50 border border-slate-200"
            }`}
          >
            Password & Security
          </button>
        </div>

        {/* Tab 1: Personal Profile */}
        {activeTab === "profile" && (
          <section className="rounded-[24px] border border-slate-200 bg-white p-8 shadow-sm">
            <h2 className="text-xl font-bold text-slate-900 mb-6">Edit Profile</h2>

            <form onSubmit={handleUpdateProfile} className="space-y-4 max-w-xl">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                    First Name
                  </label>
                  <input
                    type="text"
                    required
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    className="w-full rounded-xl border border-slate-300 p-3 text-sm outline-none focus:border-indigo-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                    Last Name
                  </label>
                  <input
                    type="text"
                    required
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    className="w-full rounded-xl border border-slate-300 p-3 text-sm outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                  Email Address
                </label>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full rounded-xl border border-slate-300 p-3 text-sm outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                  Phone Number (for M-Pesa & SMS updates)
                </label>
                <input
                  type="tel"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  placeholder="e.g. +254712345678"
                  className="w-full rounded-xl border border-slate-300 p-3 text-sm outline-none focus:border-indigo-500"
                />
              </div>

              <div className="pt-4">
                <button
                  type="submit"
                  disabled={savingProfile}
                  className="rounded-3xl bg-indigo-600 px-6 py-3 text-sm font-bold text-white shadow-md hover:bg-indigo-700 disabled:bg-slate-300 transition"
                >
                  {savingProfile ? "Saving Profile..." : "Save Profile Changes"}
                </button>
              </div>
            </form>
          </section>
        )}

        {/* Tab 2: Saved Delivery Addresses */}
        {activeTab === "addresses" && (
          <section className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-bold text-slate-900">Your Delivery Addresses</h2>
                <p className="text-xs text-slate-500">
                  Save your home, office, or favorite spots for fast 1-click checkout.
                </p>
              </div>

              <button
                type="button"
                onClick={() => {
                  resetAddressForm();
                  setShowAddAddressModal(true);
                }}
                className="rounded-3xl bg-emerald-600 px-5 py-2.5 text-xs font-bold text-white shadow-md hover:bg-emerald-700 transition flex items-center gap-1.5"
              >
                <span>+</span> Add New Address
              </button>
            </div>

            {savedAddresses.length === 0 ? (
              <div className="rounded-[24px] border border-slate-200 bg-white p-10 text-center text-slate-500">
                <p className="font-semibold text-slate-700">No saved addresses yet.</p>
                <p className="text-xs text-slate-400 mt-1">
                  Add your home or office address to checkout in seconds!
                </p>
              </div>
            ) : (
              <div className="grid gap-4 md:grid-cols-2">
                {savedAddresses.map((addr) => (
                  <div
                    key={addr.id}
                    className={`rounded-2xl border p-5 bg-white shadow-sm flex flex-col justify-between ${
                      addr.default ? "border-emerald-500 bg-emerald-50/20" : "border-slate-200"
                    }`}
                  >
                    <div>
                      <div className="flex items-center justify-between mb-2">
                        <span className="font-bold text-slate-900">
                          {addr.label}
                        </span>

                        {addr.default ? (
                          <span className="rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-bold text-emerald-800">
                            Default
                          </span>
                        ) : (
                          <button
                            type="button"
                            onClick={() => handleSetDefaultAddress(addr.id)}
                            className="text-xs font-semibold text-indigo-600 hover:underline"
                          >
                            Set Default
                          </button>
                        )}
                      </div>

                      <p className="text-sm font-medium text-slate-800">{addr.address}</p>

                      {(addr.buildingName || addr.apartmentNumber) && (
                        <p className="text-xs text-slate-600 mt-1">
                          {addr.buildingName} {addr.apartmentNumber ? `(Apt ${addr.apartmentNumber})` : ""}
                        </p>
                      )}

                      {addr.landmarks && (
                        <p className="text-xs text-slate-500 mt-0.5">
                          Landmark: {addr.landmarks}
                        </p>
                      )}

                      {addr.deliveryInstructions && (
                        <p className="text-xs italic text-amber-800 mt-1.5 bg-amber-50 p-2 rounded-lg">
                          Note: "{addr.deliveryInstructions}"
                        </p>
                      )}
                    </div>

                    <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                      {addr.latitude && addr.longitude ? (
                        <span className="text-slate-400 font-mono text-[11px]">
                          GPS Linked ({addr.latitude}, {addr.longitude})
                        </span>
                      ) : (
                        <span className="text-slate-400">Manual Address</span>
                      )}

                      <button
                        type="button"
                        onClick={() => handleDeleteAddress(addr.id)}
                        className="text-red-600 font-bold hover:underline"
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {/* Tab 3: Security */}
        {activeTab === "security" && (
          <section className="rounded-[24px] border border-slate-200 bg-white p-8 shadow-sm">
            <h2 className="text-xl font-bold text-slate-900 mb-6">Change Password</h2>

            <form onSubmit={handleChangePassword} className="space-y-4 max-w-md">
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                  Current Password
                </label>
                <input
                  type="password"
                  required
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  className="w-full rounded-xl border border-slate-300 p-3 text-sm outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                  New Password
                </label>
                <input
                  type="password"
                  required
                  minLength={6}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full rounded-xl border border-slate-300 p-3 text-sm outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                  Confirm New Password
                </label>
                <input
                  type="password"
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full rounded-xl border border-slate-300 p-3 text-sm outline-none focus:border-indigo-500"
                />
              </div>

              <div className="pt-4">
                <button
                  type="submit"
                  disabled={changingPassword}
                  className="rounded-3xl bg-indigo-600 px-6 py-3 text-sm font-bold text-white shadow-md hover:bg-indigo-700 disabled:bg-slate-300 transition"
                >
                  {changingPassword ? "Updating Password..." : "Update Password"}
                </button>
              </div>
            </form>
          </section>
        )}

        {/* Modal: Add Saved Address */}
        {showAddAddressModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm animate-fadeIn">
            <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl border border-slate-200 max-h-[90vh] overflow-y-auto">
              <div className="flex items-center justify-between pb-3 border-b border-slate-100">
                <h3 className="text-lg font-bold text-slate-900">Add New Delivery Address</h3>
                <button
                  type="button"
                  onClick={() => setShowAddAddressModal(false)}
                  className="p-1 text-slate-400 hover:text-slate-600"
                >
                  ✕
                </button>
              </div>

              <form onSubmit={handleCreateAddress} className="mt-4 space-y-3.5">
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                      Label
                    </label>
                    <select
                      value={newAddressLabel}
                      onChange={(e) => setNewAddressLabel(e.target.value)}
                      className="w-full rounded-xl border border-slate-300 p-2.5 text-sm"
                    >
                      <option value="Home">Home</option>
                      <option value="Work">Work</option>
                      <option value="Other">Other</option>
                    </select>
                  </div>

                  <div className="flex items-end">
                    <button
                      type="button"
                      disabled={locatingAddress}
                      onClick={handleDetectAddressLocation}
                      className="w-full rounded-xl border border-indigo-200 bg-indigo-50 p-2.5 text-xs font-bold text-indigo-700 hover:bg-indigo-100 disabled:bg-slate-100 transition"
                    >
                      {locatingAddress ? "Detecting GPS..." : "Use Live GPS"}
                    </button>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                    Street / Area Address <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    value={newAddressStreet}
                    onChange={(e) => setNewAddressStreet(e.target.value)}
                    placeholder="e.g. Westlands Road, Nairobi"
                    className="w-full rounded-xl border border-slate-300 p-2.5 text-sm outline-none focus:border-indigo-500"
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                      Building / Estate
                    </label>
                    <input
                      type="text"
                      value={newBuildingName}
                      onChange={(e) => setNewBuildingName(e.target.value)}
                      placeholder="e.g. Delta Towers"
                      className="w-full rounded-xl border border-slate-300 p-2.5 text-sm outline-none focus:border-indigo-500"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                      Apartment / Door #
                    </label>
                    <input
                      type="text"
                      value={newApartmentNumber}
                      onChange={(e) => setNewApartmentNumber(e.target.value)}
                      placeholder="e.g. Apt 4B"
                      className="w-full rounded-xl border border-slate-300 p-2.5 text-sm outline-none focus:border-indigo-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                    Nearby Landmarks
                  </label>
                  <input
                    type="text"
                    value={newLandmarks}
                    onChange={(e) => setNewLandmarks(e.target.value)}
                    placeholder="e.g. Opposite Shell Petrol Station"
                    className="w-full rounded-xl border border-slate-300 p-2.5 text-sm outline-none focus:border-indigo-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                    Delivery Instructions
                  </label>
                  <textarea
                    rows={2}
                    value={newDeliveryInstructions}
                    onChange={(e) => setNewDeliveryInstructions(e.target.value)}
                    placeholder="e.g. Call when outside the main security gate"
                    className="w-full rounded-xl border border-slate-300 p-2.5 text-sm outline-none focus:border-indigo-500"
                  />
                </div>

                <label className="flex items-center gap-2 cursor-pointer pt-1">
                  <input
                    type="checkbox"
                    checked={newIsDefault}
                    onChange={(e) => setNewIsDefault(e.target.checked)}
                    className="h-4 w-4 rounded text-indigo-600 focus:ring-indigo-500"
                  />
                  <span className="text-xs font-semibold text-slate-700">
                    Set as default delivery address
                  </span>
                </label>

                <div className="pt-4 flex gap-3">
                  <button
                    type="button"
                    onClick={() => setShowAddAddressModal(false)}
                    className="flex-1 rounded-2xl border border-slate-300 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    disabled={savingAddress}
                    className="flex-1 rounded-2xl bg-indigo-600 py-2.5 text-xs font-bold text-white hover:bg-indigo-700 disabled:bg-slate-300 shadow-md"
                  >
                    {savingAddress ? "Saving..." : "Save Address"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default CustomerProfilePage;