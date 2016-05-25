import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class HashTable {

    public static void main(String[] args) {

        loadCsv();

    }

    public static class user {
        Hashtable<String, String> ratings = new Hashtable<>();
    }

    public static void loadCsv () {

        String csvFile = "resources/userItem.csv.txt";
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";


        try {
            //convert all into ints

            Hashtable<Integer, user> users = new Hashtable<>();

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                String[] data = line.split(cvsSplitBy);
                int userId = new Integer(data[0]);

                user currentUser = users.get(userId);
                if (currentUser == null) {
                    currentUser = new user();
                }
                currentUser.ratings.put(data[1], data[2]);
                users.put(userId, currentUser);

            }

            int userId = 3;
            String movie = "105";
            Hashtable list = findNearestList(users, userId);
            Double recomendation = recommend(list, movie);
            System.out.println(recomendation);




        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Hashtable<Double,user> findNearestList (Hashtable<Integer, user> users, int userId){
        Hashtable<Double, user> neighbourList = new Hashtable<>();
        ArrayList<String> movieList = new ArrayList<>();
        ArrayList<String> reactionList = new ArrayList<>();
        ArrayList<String> compareList = new ArrayList<>();
        ArrayList<String> UnratedList;
        Double treshold =0.35;
        user startuser = users.get(userId);

        for (int id : users.keySet()) {
            if(id != userId) {
                movieList.addAll(startuser.ratings.keySet());
                movieList.addAll(users.get(id).ratings.keySet());
                UnratedList = getUnrated(users.get(id).ratings.keySet(),startuser.ratings.keySet());
                System.out.print("unrated movies"+UnratedList);
                if(!UnratedList.isEmpty()){
                    Set<String> uniqueMovies = new HashSet<>(movieList);
                    System.out.println("allmovies"+uniqueMovies);
                    for (String movieId : uniqueMovies) {
                        String rating = users.get(id).ratings.get(movieId);
                        if(rating == null){
                            rating = "0.0";
                        }
                        compareList.add(rating);
                        String rating2 = startuser.ratings.get(movieId);
                        if(rating2 == null){
                            rating2 = "0.0";
                        }
                        reactionList.add(rating2);
                    }
                    Double Euclidean = calcEuclidean(reactionList, compareList);
                    double distance = 1/(1+Euclidean);
                    Double Cosine = calcCosine(reactionList, compareList);
                    Double pearson = calcPearson(reactionList, compareList);
                    if (Cosine > treshold) {
                        user nearneighbour = users.get(id);
                        neighbourList = addToList(nearneighbour, neighbourList, Cosine);
                        //System.out.println("found one nearer");
                    }
                    System.out.println(reactionList);
                    System.out.println(compareList);
                    System.out.println("Euclidiean: "+distance+" Cosine: "+Cosine+" Pearson: "+ pearson);
                    compareList = new ArrayList<>();
                    reactionList = new ArrayList<>();
                    movieList = new ArrayList<>();
                }
            }
        }
        return neighbourList;
    }
    public static Double calcEuclidean(ArrayList<String> list1, ArrayList<String> list2){
        Double total = 0.0;
        for (int i=0;i<list1.size();i++){
            try {
                Double value = Double.parseDouble(list1.get(i)) - Double.parseDouble(list2.get(i));
                total += Math.pow(value, 2);
            }catch (Exception e) {
                //System.out.print("Caught the NullPointerException");
            }
        }
        return (Math.sqrt(total));
    }

    public static Double calcCosine (ArrayList<String> list1, ArrayList<String> list2) {
        Double dotProduct = 0.0;
        Double Yavg = 0.0;
        Double Xavg = 0.0;
        for (int i = 0; i < list1.size(); i++) {
            try{
                double check = Double.parseDouble(list1.get(i)) + Double.parseDouble(list2.get(i));
                dotProduct += Double.parseDouble(list1.get(i)) * Double.parseDouble((list2.get(i)));
                Yavg += Math.pow(Double.parseDouble(list1.get(i)), 2);
                Xavg += Math.pow(Double.parseDouble(list2.get(i)), 2);
            }catch (Exception e){}
        }
        return
                dotProduct / (Math.sqrt(Yavg) * Math.sqrt(Xavg));
    }
    public static Double calcPearson (ArrayList<String> list1, ArrayList<String> list2) {
        Double Yavg = 0.0;
        Double Xavg = 0.0;
        Double YavgSqrt = 0.0;
        Double XavgSqrt = 0.0;
        Double multiplied = 0.0;
        Integer count = 0;
        for (int i = 0; i < list1.size(); i++) {
            try{
                double check = Double.parseDouble(list1.get(i)) + Double.parseDouble(list2.get(i));
                Xavg += Double.parseDouble(list1.get(i));
                Yavg += Double.parseDouble(list2.get(i));
                XavgSqrt += Math.pow(Double.parseDouble(list1.get(i)), 2);
                YavgSqrt += Math.pow(Double.parseDouble(list2.get(i)), 2);
                multiplied += Double.parseDouble(list1.get(i)) * Double.parseDouble(list2.get(i));
                count += 1;
            }catch (Exception e){}
        }
        Double returnVal = (multiplied - ((Xavg * Yavg)/count))/((Math.sqrt(XavgSqrt - (Math.pow(Xavg,2)/count))) *
                (Math.sqrt(YavgSqrt - (Math.pow(Yavg,2)/count))));

        return returnVal;

    }

    public static Hashtable<Double,user> addToList(user neighbour, Hashtable<Double,user> neighbourList, double distance){
        int maxUsers = 5;
        Double lowest = 1.0;
        Double[] keyset =  neighbourList.keySet().toArray(new Double[neighbourList.keySet().size()]);
        for (double value : keyset){
            if (value < lowest){
                lowest = value;
            }
        }
        if (keyset.length < maxUsers && lowest < distance || lowest == 1.0){
            neighbourList.put(distance,neighbour);
        }
        return neighbourList;
    }

    public static ArrayList<String> getUnrated(Set<String> compare, Set<String> start){
        ArrayList<String> unrated = new ArrayList<>() ;
        for (String id :compare){
            if(!start.contains(id)){
                unrated.add(id);
            }
        }
        return unrated;
    }
    public static Double recommend(Hashtable<Double,user> neighbourList, String targetMovie){
        Double weighted = 0.0;
        Double total = 0.0;
        for (Double sim : neighbourList.keySet()){
            String rating = neighbourList.get(sim).ratings.get(targetMovie);
            if (rating != null){
                weighted += Double.parseDouble(rating) * sim;
                total += sim;
            }
        }
        return weighted/total;
    }
}
