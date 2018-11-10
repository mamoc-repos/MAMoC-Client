package uk.ac.standrews.cs.mamoc_client.Communication;

class TransferModel implements ITransferable {

    int reqCode;
    String reqType;
    String data;

    public TransferModel(int reqCode, String reqType, String data) {
        this.reqCode = reqCode;
        this.reqType = reqType;
        this.data = data;
    }

    @Override
    public int getRequestCode() {
        return reqCode;
    }

    @Override
    public String getRequestType() {
        return reqType;
    }

    @Override
    public String getData() {
        return data;
    }
}
