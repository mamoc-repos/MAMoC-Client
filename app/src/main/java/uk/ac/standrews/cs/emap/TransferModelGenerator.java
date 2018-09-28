package uk.ac.standrews.cs.emap;

class TransferModelGenerator {

    public static ITransferable generateDeviceTransferModelRequest(MamocNode device) {
        TransferModel transferModel = new TransferModel(TransferConstants.CLIENT_DATA, TransferConstants.TYPE_REQUEST,
                device.toString());
        return transferModel;
    }

    public static ITransferable generateDeviceTransferModelResponse(MamocNode currentNode) {
        TransferModel transferModel = new TransferModel(TransferConstants.CLIENT_DATA, TransferConstants.TYPE_RESPONSE, currentNode.toString());
        return transferModel;
    }

    public static ITransferable generateChatRequestModel(MamocNode device) {
        TransferModel transferModel = new TransferModel(TransferConstants.CHAT_REQUEST_SENT,
                TransferConstants.TYPE_REQUEST, device.toString());
        return transferModel;
    }

    public static ITransferable generateChatResponseModel(MamocNode device, boolean
            isChatRequestAccepted) {
        int reqCode = isChatRequestAccepted ? TransferConstants.CHAT_REQUEST_ACCEPTED :
                TransferConstants.CHAT_REQUEST_REJECTED;
        TransferModel transferModel = new TransferModel(reqCode,
                TransferConstants.TYPE_RESPONSE, device.toString());
        return transferModel;
    }
}
