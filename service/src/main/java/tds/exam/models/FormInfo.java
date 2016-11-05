package tds.exam.models;

/**
 * Created by emunoz on 11/4/16.
 */
public class FormInfo {
    private String formKey;
    private String formId;
    private Integer formLength;

    public Integer getFormLength() {
        return formLength;
    }

    public void setFormLength(Integer formLength) {
        this.formLength = formLength;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }
}
