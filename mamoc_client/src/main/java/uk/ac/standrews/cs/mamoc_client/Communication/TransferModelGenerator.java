package uk.ac.standrews.cs.mamoc_client.Communication;

import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;

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
        return new TransferModel(TransferConstants.REQUEST_SENT, TransferConstants.TYPE_REQUEST,
                device.toString());
    }

    static ITransferable generateChatResponseModel(MobileNode device, boolean isChatRequestAccepted) {
        int reqCode = isChatRequestAccepted ? TransferConstants.REQUEST_ACCEPTED :
                TransferConstants.REQUEST_REJECTED;
        return new TransferModel(reqCode, TransferConstants.TYPE_RESPONSE, device.toString());
    }
}
