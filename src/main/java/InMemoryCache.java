import java.util.*;

public class InMemoryCache {
    private Map<String, Collection<Product>> cache = new HashMap<>();


    public void updateCache(Collection<Product> products){

        cache.put("all", products);
    }
    public Collection<Product> get(){
        return cache.get("all");
    }

    public void clearMap(){
        this.cache = new HashMap<>();
    }

    public Map<String, Collection<Product>> getCache() {
        return cache;
    }
}
