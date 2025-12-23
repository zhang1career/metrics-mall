package lab.zhang.data_science.metrics_mall.common;

import lombok.Getter;

import java.util.List;


@Getter
public final class OrderedList<T> {

    private final List<T> valueList;

    public OrderedList(List<T> valueList) {
        this.valueList = List.copyOf(valueList.stream().sorted().toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderedList)) {
            return false;
        }
        OrderedList that = (OrderedList) o;
        return valueList.equals(that.valueList);
    }

    @Override
    public int hashCode() {
        return valueList.hashCode();
    }
}

