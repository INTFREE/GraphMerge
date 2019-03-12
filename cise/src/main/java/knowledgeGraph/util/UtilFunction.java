package knowledgeGraph.util;

import java.util.*;

public class UtilFunction {
    public static class CollectionUtil<E> {

        public List<E> pickRandom(Collection<E> collecion, int num) {
            List<E> copy = new ArrayList<>(collecion);
            if (copy.size() <= num) {
                return copy;
            } else {
                Collections.shuffle(copy);
                return copy.subList(0, num);
            }
        }

        public E pickRandom(Collection<E> collecion) {
            if (collecion.isEmpty()) {
                return null;
            }
            List<E> copy = new ArrayList<>(collecion);

            return copy.get(new Random().nextInt(copy.size()));
        }
    }

    public static class RandomUtil {
        public static boolean randomTest(double successRate) {
            if (successRate >= 1.0) {
                return true;
            }
            if (successRate <= 0.0) {
                return false;
            }
            return new Random().nextDouble() < successRate;
        }
    }
}
