package model;
public class allData {

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

public allData(authorData aData, bookData bData, publisherData pData,
		collectionData cData) {
	
	this.aData = aData;
	this.bData = bData;
	this.pData = pData;
	this.cData = cData;
}

public allData() {
	
}
private authorData aData;
private bookData bData;
private publisherData pData;
private collectionData cData;
}
