package st.tt.script_back.enums;

public enum RecipeResumableMode {
    YES,
    NO;

    public String toXmlValue() {
        return switch (this) {
            case YES -> "Yes";
            case NO  -> "No";
        };
    }
}