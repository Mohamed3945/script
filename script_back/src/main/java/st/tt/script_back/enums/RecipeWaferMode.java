package st.tt.script_back.enums;

public enum RecipeWaferMode {
    PRESENT,
    ABSENT,
    DONT_CARE;

    public String toXmlValue() {
        return switch (this) {
            case PRESENT    -> "Present";
            case ABSENT     -> "Absent";
            case DONT_CARE  -> "DontCare";
        };
    }
}

