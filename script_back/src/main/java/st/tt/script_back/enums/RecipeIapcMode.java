package st.tt.script_back.enums;

public enum RecipeIapcMode {
    YES,
    NO,
    REQUIRED,
    REQUIRED_IF_NO_HOST;

    public String toXmlValue() {
        return switch (this) {
            case YES      -> "Yes";
            case NO       -> "No";
            case REQUIRED -> "Required";
            case REQUIRED_IF_NO_HOST -> "RequiredIfNoHost";
        };
    }
}
