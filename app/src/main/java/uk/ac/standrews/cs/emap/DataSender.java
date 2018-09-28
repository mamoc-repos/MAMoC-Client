package uk.ac.standrews.cs.emap;

import android.content.Context;
import android.content.Intent;

class DataSender {
    public static void sendCurrentDeviceData(Context context, String destIP, int destPort,
                                             boolean isRequest) {
        MamocNode currentNode = new MamocNode();
        currentNode.setPort(Utils.getPort(context));

        String playerName = Utils.getValue(context, TransferConstants.KEY_NODE_NAME);
        if (playerName != null) {
            currentNode.setNodeName(playerName);
        }
        currentNode.setIp(Utils.getValue(context, TransferConstants.KEY_MY_IP));

        ITransferable transferData = null;
        if (!isRequest) {
            transferData = TransferModelGenerator.generateDeviceTransferModelResponse
                    (currentNode);
        } else {
            transferData = TransferModelGenerator.generateDeviceTransferModelRequest
                    (currentNode);
        }

        sendData(context, destIP, destPort, transferData);
    }

    public static void sendData(Context context, String destIP, int destPort, ITransferable data) {
        Intent serviceIntent = new Intent(context,
                DataTransferService.class);
        serviceIntent.setAction(DataTransferService.ACTION_SEND_DATA);
        serviceIntent.putExtra(
                DataTransferService.DEST_IP_ADDRESS, destIP);
        serviceIntent.putExtra(
                DataTransferService.DEST_PORT_NUMBER, destPort);

        serviceIntent.putExtra(DataTransferService.EXTRAS_SHARE_DATA, data);
        context.startService(serviceIntent);
    }

    public static void sendChatRequest(Context context, String destIP, int destPort) {
        MamocNode currentDevice = new MamocNode();
        currentDevice.setPort(Utils.getPort(context));
        String playerName = Utils.getValue(context, TransferConstants.KEY_NODE_NAME);
        if (playerName != null) {
            currentDevice.setNodeName(playerName);
        }
        currentDevice.setIp(Utils.getValue(context, TransferConstants.KEY_MY_IP));
        ITransferable transferData = TransferModelGenerator.generateChatRequestModel(currentDevice);
        sendData(context, destIP, destPort, transferData);
    }

    public static void sendChatResponse(Context context, String destIP, int destPort, boolean
            isAccepted) {
        MamocNode currentDevice = new MamocNode();
        currentDevice.setPort(Utils.getPort(context));
        String playerName = Utils.getValue(context, TransferConstants.KEY_NODE_NAME);
        if (playerName != null) {
            currentDevice.setNodeName(playerName);
        }
        currentDevice.setIp(Utils.getValue(context, TransferConstants.KEY_MY_IP));
        ITransferable transferData = TransferModelGenerator.generateChatResponseModel
                (currentDevice, isAccepted);
        sendData(context, destIP, destPort, transferData);
    }
}
