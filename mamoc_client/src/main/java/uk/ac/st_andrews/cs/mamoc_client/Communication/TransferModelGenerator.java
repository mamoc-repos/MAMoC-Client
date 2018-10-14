package uk.ac.st_andrews.cs.mamoc_client.Communication;

import uk.ac.st_andrews.cs.mamoc_client.Model.MobileNode;

class TransferModelGenerator {

    static ITransferable generateDeviceTransferModelRequest(MobileNode device) {
        return new TransferModel(TransferConstants.CLIENT_DATA, TransferConstants.TYPE_REQUEST,
                device.toString());
    }

    static ITransferable generateDeviceTransferModelResponse(MobileNode device) {
        return new TransferModel(TransferConstants.CLIENT_DATA, TransferConstants.TYPE_RESPONSE,
                device.toString());
    }

    static ITransferable generateChatRequestModel(MobileNode device) {
        return new TransferModel(TransferConstants.CHAT_REQUEST_SENT, TransferConstants.TYPE_REQUEST,
                device.toString());
    }

    static ITransferable generateChatResponseModel(MobileNode device, boolean isChatRequestAccepted) {
        int reqCode = isChatRequestAccepted ? TransferConstants.CHAT_REQUEST_ACCEPTED :
                TransferConstants.CHAT_REQUEST_REJECTED;
        return new TransferModel(reqCode, TransferConstants.TYPE_RESPONSE, device.toString());
    }
}
