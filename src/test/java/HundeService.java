import java.util.concurrent.ConcurrentHashMap;

public class HundeService {
    private final ConcurrentHashMap<String, Hund> hunde;
    private static HundeService instance;

    private HundeService(){
        hunde = new ConcurrentHashMap<>();
    }

    private static HundeService getInstance() {
        if(instance == null) instance = new HundeService();
        return instance;
    }

    public static void put(String id, Hund hund){
        getInstance().hunde.put(id, hund);
    }

    public static Hund get(String id){
        return getInstance().hunde.get(id);
    }

    public static ConcurrentHashMap<String, Hund> getAll(){
        return getInstance().hunde;
    }
}
