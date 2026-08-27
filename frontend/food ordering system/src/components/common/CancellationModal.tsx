import React, { useState, useEffect } from "react";

export interface CancellationModalProps {
  isOpen: boolean;
  title?: string;
  description?: string;
  presetReasons?: string[];
  placeholder?: string;
  confirmLabel?: string;
  isSubmitting?: boolean;
  onClose: () => void;
  onConfirm: (reason: string) => Promise<void> | void;
}

const DEFAULT_PRESETS = [
  "Changed my mind",
  "Delivery time is too long",
  "Ordered by mistake / wrong items",
  "Need to change delivery address",
  "Payment issue",
];

export const CancellationModal: React.FC<CancellationModalProps> = ({
  isOpen,
  title = "Cancel Order",
  description = "Please select or describe the reason for cancellation.",
  presetReasons = DEFAULT_PRESETS,
  placeholder = "Provide additional details (optional)...",
  confirmLabel = "Confirm Cancellation",
  isSubmitting = false,
  onClose,
  onConfirm,
}) => {
  const [selectedPreset, setSelectedPreset] = useState<string>("");
  const [customReason, setCustomReason] = useState<string>("");
  const [error, setError] = useState<string>("");

  useEffect(() => {
    if (isOpen) {
      setSelectedPreset(presetReasons[0] || "");
      setCustomReason("");
      setError("");
    }
  }, [isOpen, presetReasons]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const finalReason = customReason.trim()
      ? `${selectedPreset ? selectedPreset + ": " : ""}${customReason.trim()}`
      : selectedPreset;

    if (!finalReason.trim()) {
      setError("Please select or enter a cancellation reason.");
      return;
    }

    setError("");
    await onConfirm(finalReason);
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm animate-fadeIn"
      role="dialog"
      aria-modal="true"
      aria-labelledby="cancellation-modal-title"
    >
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl border border-slate-200">
        <div className="flex items-start justify-between pb-3 border-b border-slate-100">
          <div>
            <h3
              id="cancellation-modal-title"
              className="text-lg font-bold text-slate-950"
            >
              {title}
            </h3>
            <p className="text-xs text-slate-500 mt-0.5">{description}</p>
          </div>

          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            aria-label="Close cancellation modal"
            className="rounded-full p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition"
          >
            <svg
              className="h-5 w-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          {error && (
            <div
              role="alert"
              className="rounded-xl border border-red-200 bg-red-50 p-2.5 text-xs font-semibold text-red-700"
            >
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-2">
              Common Reasons
            </label>
            <div className="flex flex-wrap gap-1.5">
              {presetReasons.map((reason) => {
                const isSelected = selectedPreset === reason;
                return (
                  <button
                    key={reason}
                    type="button"
                    onClick={() => {
                      setSelectedPreset(reason);
                      setError("");
                    }}
                    className={`rounded-xl border px-3 py-1.5 text-xs font-medium transition text-left ${
                      isSelected
                        ? "border-red-500 bg-red-50 text-red-700 font-bold"
                        : "border-slate-200 bg-white text-slate-700 hover:border-slate-300"
                    }`}
                  >
                    {reason}
                  </button>
                );
              })}
            </div>
          </div>

          <div>
            <label
              htmlFor="cancellation-notes"
              className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1"
            >
              Additional Notes (Optional)
            </label>
            <textarea
              id="cancellation-notes"
              rows={3}
              value={customReason}
              onChange={(e) => setCustomReason(e.target.value)}
              placeholder={placeholder}
              className="w-full rounded-xl border border-slate-300 p-2.5 text-xs outline-none focus:border-red-500 focus:ring-1 focus:ring-red-200"
            />
          </div>

          <div className="flex gap-3 pt-2">
            <button
              type="button"
              disabled={isSubmitting}
              onClick={onClose}
              className="flex-1 rounded-2xl border border-slate-300 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition"
            >
              Keep Order
            </button>

            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 rounded-2xl bg-red-600 py-2.5 text-xs font-bold text-white hover:bg-red-700 disabled:bg-slate-300 shadow-md shadow-red-600/20 transition flex items-center justify-center gap-2"
            >
              {isSubmitting ? (
                <>
                  <svg
                    className="h-4 w-4 animate-spin"
                    viewBox="0 0 24 24"
                    fill="none"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    />
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8v8H4z"
                    />
                  </svg>
                  <span>Processing...</span>
                </>
              ) : (
                confirmLabel
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CancellationModal;

