package com.foodordering.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
            this.stkCallback =
                    stkCallback;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StkCallback {

        @JsonProperty("CheckoutRequestID")
        private String checkoutRequestId;

        @JsonProperty("ResultCode")
        private Integer resultCode;

        @JsonProperty("ResultDesc")
        private String resultDescription;

        public String getCheckoutRequestId() {
            return checkoutRequestId;
        }

        public void setCheckoutRequestId(
                String checkoutRequestId
        ) {
            this.checkoutRequestId =
                    checkoutRequestId;
        }

        public Integer getResultCode() {
            return resultCode;
        }

        public void setResultCode(
                Integer resultCode
        ) {
            this.resultCode =
                    resultCode;
        }

        public String getResultDescription() {
            return resultDescription;
        }

        public void setResultDescription(
                String resultDescription
        ) {
            this.resultDescription =
                    resultDescription;
        }

        public boolean isSuccessful() {
            return resultCode != null
                    && resultCode == 0;
        }
    }
}
