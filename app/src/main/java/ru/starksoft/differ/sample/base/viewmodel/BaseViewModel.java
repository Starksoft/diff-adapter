package ru.starksoft.differ.sample.base.viewmodel;

import ru.starksoft.differ.sample.base.divider.SeparatorType;
import ru.starksoft.differ.viewmodel.DifferViewModel;

public abstract class BaseViewModel extends DifferViewModel {

    public BaseViewModel(int contentHashCode) {
        super(contentHashCode);
    }

    /**
     * Тип divider, который будет рисовать под этим элементом в списке
     *
     * @return тип divider
     */
    @Override
    @SeparatorType
    public int getDividerType() {
        return SeparatorType.SEPARATOR_WITHOUT_PADDING;
    }

}
