package uk.ac.standrews.cs.mamoc_client.Communication;

import java.io.Serializable;

interface ITransferable extends Serializable {
    int getRequestCode();
    String getRequestType();
    String getData();
}
