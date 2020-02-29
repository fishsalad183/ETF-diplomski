package concepts;

import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.Animation;

public interface Animated {
    
    default List<Animation> getAnimations() {
        if (this instanceof GameObject) {
            List<Animation> list = ((GameObject) this).getChildren()
                    .stream()
                    .filter(n -> n instanceof Animated)
                    .flatMap(n -> ((Animated) n).getAnimations().stream())
                    .collect(Collectors.toList());
            return list;
        }
        return null;
    }
    
}
