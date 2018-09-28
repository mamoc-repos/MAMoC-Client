package uk.ac.standrews.cs.emap;

import java.io.Serializable;

public interface ITransferable extends Serializable {
    int getRequestCode();
    String getRequestType();
    String getData();
}
