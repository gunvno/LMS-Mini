package vn.com.atomi.charge.base.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseDto<I> {

    @Null(groups = Create.class)
    @NotNull(groups = Nested.class)
    @Positive(groups = Nested.class)
    public I id;

    public interface Create {

    }

    public interface Update {

    }

    public interface Nested {

    }
}
