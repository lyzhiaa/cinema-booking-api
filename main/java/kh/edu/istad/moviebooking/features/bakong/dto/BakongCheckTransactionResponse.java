package kh.edu.istad.moviebooking.features.bakong.dto;

public record BakongCheckTransactionResponse(
        Integer responseCode,

        String responseMessage,

        Integer errorCode,

        BakongTransactionData data
) {
}
