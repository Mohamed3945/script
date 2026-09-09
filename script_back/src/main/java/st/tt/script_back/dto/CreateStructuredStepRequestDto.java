package st.tt.script_back.dto;

import st.tt.script_back.enums.StepKind;

public class CreateStructuredStepRequestDto {

    private String name;
    private StepKind stepKind;
    private Integer orderIndex;

    public CreateStructuredStepRequestDto() {
    }

    public CreateStructuredStepRequestDto(String name, StepKind stepKind, Integer orderIndex) {
        this.name = name;
        this.stepKind = stepKind;
        this.orderIndex = orderIndex;
    }

    public String getName() {
        return name;
    }

    public StepKind getStepKind() {
        return stepKind;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStepKind(StepKind stepKind) {
        this.stepKind = stepKind;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}