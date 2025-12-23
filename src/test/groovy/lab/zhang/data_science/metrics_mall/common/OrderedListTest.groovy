package lab.zhang.data_science.metrics_mall.common

import spock.lang.Specification

/**
 * Test for OrderedKey.
 *
 * @author Rongjin Zhang
 */
class OrderedListTest extends Specification {

    def "test equals with same elements but different order - String"() {
        given:
        def list1 = ["c", "a", "b"]
        def list2 = ["b", "c", "a"]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 == key2
        key1.hashCode() == key2.hashCode()
    }

    def "test equals with same elements but different order - Integer"() {
        given:
        def list1 = [3, 1, 2]
        def list2 = [2, 3, 1]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 == key2
        key1.hashCode() == key2.hashCode()
    }

    def "test equals with same elements but different order - Long"() {
        given:
        def list1 = [300L, 100L, 200L]
        def list2 = [200L, 300L, 100L]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 == key2
        key1.hashCode() == key2.hashCode()
    }

    def "test not equals with different elements - String"() {
        given:
        def list1 = ["a", "b", "c"]
        def list2 = ["a", "b", "d"]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 != key2
    }

    def "test not equals with different elements - Integer"() {
        given:
        def list1 = [1, 2, 3]
        def list2 = [1, 2, 4]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 != key2
    }

    def "test equals with empty list"() {
        given:
        def list1 = []
        def list2 = []

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 == key2
        key1.hashCode() == key2.hashCode()
    }

    def "test equals with single element"() {
        given:
        def list1 = ["a"]
        def list2 = ["a"]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 == key2
        key1.hashCode() == key2.hashCode()
    }

    def "test not equals with single element different value"() {
        given:
        def list1 = ["a"]
        def list2 = ["b"]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 != key2
    }

    def "test equals with duplicate elements"() {
        given:
        def list1 = ["a", "b", "a", "c"]
        def list2 = ["c", "a", "b", "a"]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 == key2
        key1.hashCode() == key2.hashCode()
    }

    def "test equals with already sorted list"() {
        given:
        def list1 = ["a", "b", "c"]
        def list2 = ["a", "b", "c"]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 == key2
        key1.hashCode() == key2.hashCode()
    }

    def "test equals with reverse sorted list"() {
        given:
        def list1 = ["a", "b", "c"]
        def list2 = ["c", "b", "a"]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 == key2
        key1.hashCode() == key2.hashCode()
    }

    def "test equals with different list sizes"() {
        given:
        def list1 = ["a", "b", "c"]
        def list2 = ["a", "b"]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 != key2
    }

    def "test hashCode consistency"() {
        given:
        def list1 = ["c", "a", "b"]
        def list2 = ["b", "c", "a"]
        def list3 = ["a", "b", "c"]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)
        def key3 = new OrderedList<>(list3)

        then:
        key1.hashCode() == key2.hashCode()
        key2.hashCode() == key3.hashCode()
        key1.hashCode() == key3.hashCode()
    }

    def "test throws exception with null list element"() {
        given:
        def list1 = ["a", null, "b"]

        when:
        new OrderedList<>(list1)

        then:
        thrown(NullPointerException)
    }

    def "test equals with custom object type"() {
        given:
        def list1 = [new TestObject("c"), new TestObject("a"), new TestObject("b")]
        def list2 = [new TestObject("b"), new TestObject("c"), new TestObject("a")]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        thrown(ClassCastException)
    }

    def "test equals with comparable object type"() {
        given:
        def list1 = [new ComparableObject("c"), new ComparableObject("a"), new ComparableObject("b")]
        def list2 = [new ComparableObject("b"), new ComparableObject("c"), new ComparableObject("a")]

        when:
        def key1 = new OrderedList<>(list1)
        def key2 = new OrderedList<>(list2)

        then:
        key1 == key2
        key1.hashCode() == key2.hashCode()
    }

    def "test equals with same reference"() {
        given:
        def list = ["a", "b", "c"]
        def key1 = new OrderedList<>(list)

        when:
        def result = key1.equals(key1)

        then:
        result
    }

    def "test equals with different type"() {
        given:
        def list = ["a", "b", "c"]
        def key1 = new OrderedList<>(list)

        when:
        def result = key1.equals("not an OrderedKey")

        then:
        !result
    }

    def "test equals with null"() {
        given:
        def list = ["a", "b", "c"]
        def key1 = new OrderedList<>(list)

        when:
        def result = key1.equals(null)

        then:
        !result
    }

    static class TestObject {
        String value

        TestObject(String value) {
            this.value = value
        }
    }

    static class ComparableObject implements Comparable<ComparableObject> {
        String value

        ComparableObject(String value) {
            this.value = value
        }

        @Override
        int compareTo(ComparableObject o) {
            return value.compareTo(o.value)
        }

        @Override
        boolean equals(Object o) {
            if (this == o) return true
            if (o == null || getClass() != o.getClass()) return false
            ComparableObject that = (ComparableObject) o
            return value == that.value
        }

        @Override
        int hashCode() {
            return value != null ? value.hashCode() : 0
        }
    }
}

