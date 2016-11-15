/**
 * Created by feng.li on 2016/11/14.
 */
public class ProductEntity {

    //目标网商品编号

    public String getProductEnglishName() {
        return productEnglishName;
    }

    public void setProductEnglishName(String productEnglishName) {
        this.productEnglishName = productEnglishName;
    }

    public String getProductShortEnglishName() {
        return productShortEnglishName;
    }

    public void setProductShortEnglishName(String productShortEnglishName) {
        this.productShortEnglishName = productShortEnglishName;
    }

    public String getProductChineseName() {
        return productChineseName;
    }

    public void setProductChineseName(String productChineseName) {
        this.productChineseName = productChineseName;
    }

    public String getProductShortChineseName() {
        return productShortChineseName;
    }

    public void setProductShortChineseName(String productShortChineseName) {
        this.productShortChineseName = productShortChineseName;
    }

    public String getProductingArea() {
        return productingArea;
    }

    public void setProductingArea(String productingArea) {
        this.productingArea = productingArea;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getOriginAmount() {
        return originAmount;
    }

    public void setOriginAmount(String originAmount) {
        this.originAmount = originAmount;
    }

    public String getNowAmount() {
        return nowAmount;
    }

    public void setNowAmount(String nowAmount) {
        this.nowAmount = nowAmount;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getUnitContent() {
        return unitContent;
    }

    public void setUnitContent(String unitContent) {
        this.unitContent = unitContent;
    }

    public String getProductDescribe() {
        return productDescribe;
    }

    public void setProductDescribe(String productDescribe) {
        this.productDescribe = productDescribe;
    }

    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    public String getFunctionDescripe() {
        return functionDescripe;
    }

    public void setFunctionDescripe(String functionDescripe) {
        this.functionDescripe = functionDescripe;
    }

    public String getMainContent() {
        return mainContent;
    }

    public void setMainContent(String mainContent) {
        this.mainContent = mainContent;
    }

    public String getIntendedFor() {
        return intendedFor;
    }

    public void setIntendedFor(String intendedFor) {
        this.intendedFor = intendedFor;
    }

    public String getUsageMethod() {
        return usageMethod;
    }

    public void setUsageMethod(String usageMethod) {
        this.usageMethod = usageMethod;
    }

    public String getAttention() {
        return attention;
    }

    public void setAttention(String attention) {
        this.attention = attention;
    }

    private String productId;
    private String productEnglishName;
    private String productShortEnglishName;
    private String productChineseName;
    private String productShortChineseName;
    //产地
    private String productingArea;
    //商品重量
    private int weight;
    private String originAmount;
    private String nowAmount;
    //功效
    private String function;
    //单位含量
    private String unitContent;
    //产品描述
    private String productDescribe;
    //产品特点
    private String characteristic;
    //功能描述
    private String functionDescripe;
    //主要成分
    private String mainContent;
    //适用人群
    private String intendedFor;
    //使用方法
    private String usageMethod;
    //注意事项
    private String attention;
    //目标产品URL
    private String productUrl;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }
}
