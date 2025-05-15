import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class InventoryService {

    public void initDataDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    public List<Product> loadProducts(Path csvPath) throws IOException {
        List<Product> products = new ArrayList<>();
        if (Files.exists(csvPath)) {
            try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
                String line = reader.readLine(); // skip header
                while ((line = reader.readLine()) != null) {
                    products.add(Product.fromCsvLine(line));
                }
            }
        }
        return products;
    }

    public void saveProducts(List<Product> products, Path csvPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(csvPath)) {
            writer.write("id,name,category,price,quantity");
            writer.newLine();
            for (Product p : products) {
                writer.write(p.toCsvLine());
                writer.newLine();
            }
        }
    }

    public List<Product> searchProducts(List<Product> products, String keyword) {
        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void sortProducts(List<Product> products, String criteria) {
        if (criteria.equalsIgnoreCase("price")) {
            products.sort(Comparator.comparingDouble(Product::getPrice));
        } else if (criteria.equalsIgnoreCase("quantity")) {
            products.sort(Comparator.comparingInt(Product::getQuantity));
        }
    }

    public List<Product> filterByPrice(List<Product> products, double min, double max) {
        return products.stream()
                .filter(p -> p.getPrice() >= min && p.getPrice() <= max)
                .collect(Collectors.toList());
    }

    public void runMenu(List<Product> products) throws IOException {
        Scanner sc = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n=== INVENTORY MANAGER ===");
            System.out.println("1. Lihat semua");
            System.out.println("2. Tambah produk");
            System.out.println("3. Update stok");
            System.out.println("4. Hapus produk");
            System.out.println("5. Cari produk");
            System.out.println("6. Sort produk");
            System.out.println("7. Filter harga");
            System.out.println("8. Simpan & Keluar");
            System.out.print("Pilih: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    viewAll(products);
                    break;
                case "2":
                    addProduct(products, sc);
                    break;
                case "3":
                    updateQuantity(products, sc);
                    break;
                case "4":
                    deleteProduct(products, sc);
                    break;
                case "5":
                    System.out.print("Masukkan keyword nama produk: ");
                    String keyword = sc.nextLine();
                    List<Product> hasil = searchProducts(products, keyword);
                    viewAll(hasil);
                    break;
                case "6":
                    System.out.print("Sort berdasarkan (price/quantity): ");
                    String crit = sc.nextLine();
                    sortProducts(products, crit);
                    viewAll(products);
                    break;
                case "7":
                    System.out.print("Harga minimum: ");
                    double min = Double.parseDouble(sc.nextLine());
                    System.out.print("Harga maksimum: ");
                    double max = Double.parseDouble(sc.nextLine());
                    List<Product> filtered = filterByPrice(products, min, max);
                    viewAll(filtered);
                    break;
                case "8":
                    running = false;
                    break;
                default:
                    System.out.println("Pilihan tidak valid.");
            }
        }
    }

    private void viewAll(List<Product> products) {
        System.out.println("\nDaftar Produk:");
        for (Product p : products) {
            System.out.printf("%d | %s | %s | %.2f | %d\n",
                    p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getQuantity());
        }
    }

    private void addProduct(List<Product> products, Scanner sc) {
        System.out.print("ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("Nama: ");
        String name = sc.nextLine();
        System.out.print("Kategori: ");
        String category = sc.nextLine();
        System.out.print("Harga: ");
        double price = Double.parseDouble(sc.nextLine());
        System.out.print("Kuantitas: ");
        int quantity = Integer.parseInt(sc.nextLine());

        products.add(new Product(id, name, category, price, quantity));
        System.out.println("Produk berhasil ditambahkan.");
    }

    private void updateQuantity(List<Product> products, Scanner sc) {
        System.out.print("ID produk yang ingin diperbarui: ");
        int id = Integer.parseInt(sc.nextLine());
        for (Product p : products) {
            if (p.getId() == id) {
                System.out.print("Stok baru: ");
                int qty = Integer.parseInt(sc.nextLine());
                p.setQuantity(qty);
                System.out.println("Stok berhasil diperbarui.");
                return;
            }
        }
        System.out.println("Produk tidak ditemukan.");
    }

    private void deleteProduct(List<Product> products, Scanner sc) {
        System.out.print("ID produk yang ingin dihapus: ");
        int id = Integer.parseInt(sc.nextLine());
        Iterator<Product> iter = products.iterator();
        while (iter.hasNext()) {
            if (iter.next().getId() == id) {
                iter.remove();
                System.out.println("Produk berhasil dihapus.");
                return;
            }
        }
        System.out.println("Produk tidak ditemukan.");
    }
}
