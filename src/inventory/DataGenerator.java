package inventory;
import java.util.Random;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DataGenerator {
    public static void main(String[] args){
        Random randStd = new Random(22202963);

        String[] categories = {"Electronics", "Groceries", "Books", "Clothing"};
        String[] paths = {
            "src/inventory/Electronics.txt",
            "src/inventory/Groceries.txt",
            "src/inventory/Books.txt",
            "src/inventory/Clothing.txt"
        };
        
        for (int i = 0; i < 4; i++){
            System.out.println(categories[i]);
            try(PrintWriter pw = new PrintWriter(new FileWriter(paths[i]))){
                for(int j = 0; j < 30; j++){
                    char Fletter = categories[i].charAt(0);
                    String prodID = "" + Fletter + (1001 + j);
                    String prodName = categories[i] + "_Product_" + (j+1);
                    int price = randStd.nextInt(500) + 1; //generating random price from 1 to 500

                    pw.println(prodID + ", " + prodName + ", " + price + ".00");
                }
                pw.println("P9999, " + "Student_22202963_Demo, " + 0.01);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            System.out.println("Created: " + paths[i]);
        }
    }
}
