package com.foodordering.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MpesaCallbackRequest {

    @JsonProperty("Body")
    private Body body;

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public StkCallback getStkCallback() {
        return body != null
                ? body.getStkCallback()
                : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {

        @JsonProperty("stkCallback")
        private StkCallback stkCallback;

        public StkCallback getStkCallback() {
            return stkCallback;
        }

        public void setStkCallback(
                StkCallback stkCallback
        ) {
            this.stkCallback = stkCallback;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StkCallback {

        @JsonProperty("MerchantRequestID")
        private String merchantRequestId;

        @JsonProperty("CheckoutRequestID")
        private String checkoutRequestId;

        @JsonProperty("ResultCode")
        private Integer resultCode;

        @JsonProperty("ResultDesc")
        private String resultDescription;

        @JsonProperty("CallbackMetadata")
        private CallbackMetadata callbackMetadata;

        public String getMerchantRequestId() {
            return merchantRequestId;
        }

        public void setMerchantRequestId(String merchantRequestId) {
            this.merchantRequestId = merchantRequestId;
        }

        public String getCheckoutRequestId() {
            return checkoutRequestId;
        }

        public void setCheckoutRequestId(
                String checkoutRequestId
        ) {
            this.checkoutRequestId = checkoutRequestId;
        }

        public Integer getResultCode() {
            return resultCode;
        }

        public void setResultCode(
                Integer resultCode
        ) {
            this.resultCode = resultCode;
        }

        public String getResultDescription() {
            return resultDescription;
        }

        public void setResultDescription(
                String resultDescription
        ) {
            this.resultDescription = resultDescription;
        }

        public CallbackMetadata getCallbackMetadata() {
            return callbackMetadata;
        }

        public void setCallbackMetadata(CallbackMetadata callbackMetadata) {
            this.callbackMetadata = callbackMetadata;
        }

        public boolean isSuccessful() {
            return resultCode != null && resultCode == 0;
        }

        public BigDecimal getAmount() {
            if (callbackMetadata == null || callbackMetadata.getItem() == null) {
                return null;
            }
            for (Item item : callbackMetadata.getItem()) {
                if ("Amount".equalsIgnoreCase(item.getName()) && item.getValue() != null) {
                    try {
                        return new BigDecimal(String.valueOf(item.getValue()).trim());
                    } catch (Exception ignored) {}
                }
            }
            return null;
        }

        public String getMpesaReceiptNumber() {
            if (callbackMetadata == null || callbackMetadata.getItem() == null) {
                return null;
            }
            for (Item item : callbackMetadata.getItem()) {
                if ("MpesaReceiptNumber".equalsIgnoreCase(item.getName()) && item.getValue() != null) {
                    return String.valueOf(item.getValue()).trim();
                }
            }
            return null;
        }

        public String getPhoneNumber() {
            if (callbackMetadata == null || callbackMetadata.getItem() == null) {
                return null;
            }
            for (Item item : callbackMetadata.getItem()) {
                if ("PhoneNumber".equalsIgnoreCase(item.getName()) && item.getValue() != null) {
                    return String.valueOf(item.getValue()).trim();
                }
            }
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CallbackMetadata {

        @JsonProperty("Item")
        private List<Item> item;

        public List<Item> getItem() {
            return item;
        }

        public void setItem(List<Item> item) {
            this.item = item;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        @JsonProperty("Name")
        private String name;

        @JsonProperty("Value")
        private Object value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
