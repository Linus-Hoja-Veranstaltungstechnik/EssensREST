import java.io.Serializable;
import java.util.Objects;

public class Hund implements Serializable {
    private String name;
    private Integer age;

    public Hund() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hund hund = (Hund) o;
        return Objects.equals(name, hund.name) && Objects.equals(age, hund.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }

    @Override
    public String toString() {
        return "Hund{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
