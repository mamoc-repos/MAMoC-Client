package uk.ac.st_andrews.cs.mamoc_client.Communication;

import uk.ac.st_andrews.cs.mamoc_client.Model.MamocNode;

class TransferModelGenerator {

    static ITransferable generateDeviceTransferModelRequest(MamocNode device) {
        return new TransferModel(TransferConstants.CLIENT_DATA, TransferConstants.TYPE_REQUEST,
                device.toString());
    }

    static ITransferable generateDeviceTransferModelResponse(MamocNode device) {
        return new TransferModel(TransferConstants.CLIENT_DATA, TransferConstants.TYPE_RESPONSE,
                device.toString());
    }

    static ITransferable generateChatRequestModel(MamocNode device) {
        return new TransferModel(TransferConstants.CHAT_REQUEST_SENT, TransferConstants.TYPE_REQUEST,
                device.toString());
    }

    static ITransferable generateChatResponseModel(MamocNode device, boolean isChatRequestAccepted) {
        int reqCode = isChatRequestAccepted ? TransferConstants.CHAT_REQUEST_ACCEPTED :
                TransferConstants.CHAT_REQUEST_REJECTED;
        return new TransferModel(reqCode, TransferConstants.TYPE_RESPONSE, device.toString());
    }
}
