package org.acquire.models;

import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;

import java.util.*;
import java.util.stream.Collectors;

public class Banker {

    private Set<Tile> tilePool;

    private static Banker banker = null;

    private Random random;
    private static final Map<HotelLabel, String> hotelTiers = new HashMap<>();
    // Pricing table for each tier
    private static final Map<String, TreeMap<Integer, Integer>> pricingTable = new HashMap<>();

    private Map<HotelLabel,Integer> hotelShare = new HashMap<>();

    static {



        // Initialize hotel tiers
        hotelTiers.put(HotelLabel.Worldwide, "Tier1");
        hotelTiers.put(HotelLabel.Sackson, "Tier1");
        hotelTiers.put(HotelLabel.Festival, "Tier2");
        hotelTiers.put(HotelLabel.Imperial, "Tier2");
        hotelTiers.put(HotelLabel.American, "Tier2");
        hotelTiers.put(HotelLabel.Continental, "Tier3");
        hotelTiers.put(HotelLabel.Tower, "Tier3");

        // Initialize pricing tables for each tier
        TreeMap<Integer, Integer> tier1Prices = new TreeMap<>();
        TreeMap<Integer, Integer> tier2Prices = new TreeMap<>();
        TreeMap<Integer, Integer> tier3Prices = new TreeMap<>();

        tier1Prices.put(2, 200);
        tier1Prices.put(3, 300);
        tier1Prices.put(4, 400);
        tier1Prices.put(5, 500);
        tier1Prices.put(6, 600);
        tier1Prices.put(11, 700);
        tier1Prices.put(21, 800);
        tier1Prices.put(31, 900);
        tier1Prices.put(41, 1000);

//        tier2Prices.put(0, 200);
        tier2Prices.put(2, 300);
        tier2Prices.put(3, 400);
        tier2Prices.put(4, 500);
        tier2Prices.put(5, 600);
        tier2Prices.put(6, 700);
        tier2Prices.put(11, 800);
        tier2Prices.put(21, 900);
        tier2Prices.put(31, 1000);
        tier2Prices.put(41, 1100);

//        tier3Prices.put(0, 200);
        tier3Prices.put(2, 400);
        tier3Prices.put(3, 500);
        tier3Prices.put(4, 600);
        tier3Prices.put(5, 700);
        tier3Prices.put(6, 800);
        tier3Prices.put(11, 900);
        tier3Prices.put(21, 1000);
        tier3Prices.put(31, 1100);
        tier3Prices.put(41, 1200);

        pricingTable.put("Tier1", tier1Prices);
        pricingTable.put("Tier2", tier2Prices);
        pricingTable.put("Tier3", tier3Prices);
    }

    private Banker(){
        BoardRow[] rows = BoardRow.values();
        BoardColumn[] columns = BoardColumn.values();
        tilePool = new HashSet<>(rows.length*columns.length);
        random = new Random(System.currentTimeMillis());
        for(BoardRow row:rows){
            for(BoardColumn column:columns){
                tilePool.add((new Tile(row,column)));
            }
        }
        // Initialize hotel shares count
        // code refactor for better scaling
        for(HotelLabel label:HotelLabel.values()){
            hotelShare.put(label, 25);
        }
//        hotelShare.put(HotelLabel.Tower,25);
//        hotelShare.put(HotelLabel.Worldwide,25);
//        hotelShare.put(HotelLabel.Sackson,25);
//        hotelShare.put(HotelLabel.American,25);
//        hotelShare.put(HotelLabel.Imperial,25);
//        hotelShare.put(HotelLabel.Continental,25);
//        hotelShare.put(HotelLabel.Festival,25);
    }

    public Banker(Banker other) {
        random = new Random();  // Create a new Random instance
        tilePool = new HashSet<>(other.tilePool);
        hotelShare = new HashMap<>(other.hotelShare);
    }

    public static void destroyBanker(){
        banker.tilePool = null;
        banker = null;
    }
    public static Banker getBanker(){
        if(banker==null){
            banker = new Banker();
        }
        return banker;
    }

    public Tile getRandomTile(){
        if(tilePool.size()==0){
            return null;
        }
        ArrayList<Tile> tiles = new ArrayList<>(tilePool.parallelStream().collect(Collectors.toList()));

        Tile selectedTile = tiles.get(random.nextInt(tiles.size()));

        tilePool.remove(selectedTile);
        return selectedTile;
    }
    public int getCertificatePrice(HotelLabel hotelLabel, int tiles) {
        // Determine the tier of the hotel
        String tier = hotelTiers.get(hotelLabel);
        if (tier == null) {
            throw new RuntimeException("Unknown hotel name: " + hotelLabel);
        }

        if (tiles<2) {
            throw new RuntimeException("You cannot buy a share for hotel:"+ hotelLabel.toString()+" with tiles:"+tiles);
        }

        // Get the pricing table for the tier
        TreeMap<Integer, Integer> prices = pricingTable.get(tier);
        Map.Entry<Integer, Integer> priceEntry = prices.floorEntry(tiles);


        // Return the price
        return priceEntry.getValue();
    }

    public Set<Tile> getTilePool() {
        return tilePool;
    }

    public void setTilePool(Set<Tile> tilePool) {
        this.tilePool = tilePool;
    }

    public void setHotelShare(Map<HotelLabel,Integer> hShare){ hotelShare = hShare; }

    public int getCurrentHotelShareCount(HotelLabel label){
        return hotelShare.get(label);
    }

    public void decrementHotelShareCount(HotelLabel label,int decrement){
        hotelShare.put(label, (hotelShare.get(label)-decrement));
    }

//    public static void main(String[] args) {
//        Banker banker1 = Banker.getBanker();
//        for(int i=0;i<6;i++)System.out.println(banker1.getRandomTile());
//    }


}
