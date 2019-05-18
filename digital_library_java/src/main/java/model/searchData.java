package model;

public class searchData {

    public authorData getaData() {
        return aData;
    }

    public void setaData(authorData aData) {
        this.aData = aData;
    }

    public bookData getbData() {
        return bData;
    }

    public void setbData(bookData bData) {
        this.bData = bData;
    }

    public publisherData getpData() {
        return pData;
    }

    public void setpData(publisherData pData) {
        this.pData = pData;
    }

    public collectionData getcData() {
        return cData;
    }

    public void setcData(collectionData cData) {
        this.cData = cData;
    }

    public searchData(authorData aData, bookData bData, publisherData pData,
                      collectionData cData) {
        super();
        this.aData = aData;
        this.bData = bData;
        this.pData = pData;
        this.cData = cData;
    }

    private authorData aData;

    public searchData() {

    }

    private bookData bData;
    private publisherData pData;
    private collectionData cData;
}
