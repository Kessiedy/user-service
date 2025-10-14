package com.userservice;

import com.userservice.entity.User;
import com.userservice.exception.UserAlreadyExistsException;
import com.userservice.exception.UserNotFoundException;
import com.userservice.exception.ValidationException;
import com.userservice.service.UserService;
import com.userservice.service.UserServiceImpl;
import com.userservice.util.HibernateUtil;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;


public class Main {

    public static final UserService userService = new UserServiceImpl();
    public static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║     СИСТЕМА УПРАВЛЕНИЯ ПОЛЬЗОВАТЕЛЯМИ      ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.println();

        boolean running = true;

        while (running) {
            try {
                showMenu();
                int choice = getUserChoice();

                switch (choice) {
                    case 1:
                        createUser();
                        break;
                    case 2:
                        showAllUsers();
                        break;
                    case 3:
                        findUserById();
                        break;
                    case 4:
                        findUserByEmail();
                        break;
                    case 5:
                        updateUser();
                        break;
                    case 6:
                        deleteUser();
                        break;
                    case 7:
                        showUserCount();
                        break;
                    case 8:
                        deleteAllUsers();
                        break;
                    case 0:
                        running = false;
                        System.out.println("\n👋 Выход из программы. До свидания!");
                        break;
                    default:
                        System.out.println("\nX Неверный выбор. Попробуйте снова.");
                }
                if (running && choice != 0) {
                    System.out.println("\nНажмите Enter для продолжения...");
                    scanner.nextLine();
                }
            } catch (Exception e) {
                System.err.println("\nX Произошла ошибка: " + e.getMessage());
                scanner.nextLine();
            }
        }
        scanner.close();
        HibernateUtil.shutdown();
    }

    private static void showMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("                 ГЛАВНОЕ МЕНЮ");
        System.out.println("=".repeat(50));
        System.out.println("1. ➕ Создать пользователя");
        System.out.println("2. 📋 Показать всех пользователей");
        System.out.println("3. 🔍 Найти пользователя по ID");
        System.out.println("4. 📧 Найти пользователя по Email");
        System.out.println("5. ✏️  Обновить пользователя");
        System.out.println("6. 🗑️  Удалить пользователя");
        System.out.println("7. 📊 Показать количество пользователей");
        System.out.println("8. ⚠️  Удалить всех пользователей");
        System.out.println("0. 🚪 Выход");
        System.out.println("=".repeat(50));
        System.out.print("Ваш выбор: ");
    }

    private static int getUserChoice() {
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            return choice;
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return -1;
        }
    }

    public static void createUser() {
        System.out.println("\n" + "─".repeat(50));
        System.out.println("         СОЗДАНИЕ НОВОГО ПОЛЬЗОВАТЕЛЯ");
        System.out.println("─".repeat(50));

        try {
            System.out.print("Введите имя: ");
            String name = scanner.nextLine();

            System.out.print("Введите email: ");
            String email = scanner.nextLine();

            System.out.print("Введите возраст (или оставьте пустым): ");
            String ageInput = scanner.nextLine();
            Integer age = null;

            if (!ageInput.trim().isEmpty()) {
                try {
                    age = Integer.parseInt(ageInput);
                } catch (NumberFormatException e) {
                    System.out.println("⚠️  Некорректный возраст. Будет установлен как NULL.");

                }

            }

            User user = userService.createUser(name, email, age);
            System.out.println("\nПользователь успешно создан!");
            printUserDetails(user);
        } catch (ValidationException e) {
            System.out.println("\nОшибка валидации: " + e.getMessage());
        } catch (UserAlreadyExistsException e) {
            System.out.println("\nX" + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nОшибка при создании пользователя: " + e.getMessage());
        }
    }

    private static void showAllUsers() {
        System.out.println("\n" + "─".repeat(50));
        System.out.println("            СПИСОК ВСЕХ ПОЛЬЗОВАТЕЛЕЙ");
        System.out.println("─".repeat(50));

        try {
            List<User> users = userService.getAllUsers();

            if (users.isEmpty()) {
                System.out.println("\nСписок пользователей пуст.");
            } else {
                System.out.println("\nНайдено пользователей: " + users.size());
                System.out.println();
                printUsersTable(users);
            }
        } catch (Exception e) {
            System.out.println("\nОшибка при получении списка: " + e.getMessage());
        }
    }

    private static void findUserById() {
        System.out.println("\n" + "─".repeat(50));
        System.out.println("           ПОИСК ПОЛЬЗОВАТЕЛЯ ПО ID");
        System.out.println("─".repeat(50));

        try {
            System.out.print("Введите ID пользователя: ");
            Long id = scanner.nextLong();
            scanner.nextLine();

            User user = userService.getUserById(id);

            System.out.println("\nПользователь найден!");
            printUserDetails(user);
        } catch (InputMismatchException e) {
            scanner.nextLine();
            System.out.println("\nID должен быть числом");
        } catch (UserNotFoundException e) {
            System.out.println("\nX " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nОшибка при поиске: " + e.getMessage());
        }
    }

    private static void findUserByEmail() {
        System.out.println("\n" + "─".repeat(50));
        System.out.println("          ПОИСК ПОЛЬЗОВАТЕЛЯ ПО EMAIL");
        System.out.println("─".repeat(50));

        try {
            System.out.print("Введите email: ");
            String email = scanner.nextLine();

            User user = userService.getUserByEmail(email);
            System.out.println("\nПользователь найден!");
            printUserDetails(user);
        } catch (UserNotFoundException e) {
            System.out.println("\nx " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nx Ошибка при поиске: " + e.getMessage());
        }
    }

    private static void updateUser() {
        System.out.println("\n" + "─".repeat(50));
        System.out.println("           ОБНОВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ");
        System.out.println("─".repeat(50));

        try {
            System.out.print("Введите ID пользователя для обновления: ");
            Long id = scanner.nextLong();

            User currentUser = userService.getUserById(id);
            System.out.println("\nТекущие данные:");
            printUserDetails(currentUser);

            System.out.println("\nОставьте поле пустым, если не хотите его изменять.");
            System.out.print("Новое имя [" + currentUser.getName() + "]: ");
            String name = scanner.nextLine();

            System.out.print("Новый email [" + currentUser.getEmail() + "]: ");
            String email = scanner.nextLine();

            System.out.print("Новый возраст [" + currentUser.getAge() + "]: ");
            String ageInput = scanner.nextLine();
            Integer age = null;

            if (!ageInput.trim().isEmpty()) {
                try {
                    age = Integer.parseInt(ageInput);
                } catch (NumberFormatException e) {
                    System.out.println("Некорректный возраст. Изменение будет пропущено.");

                }
            }
            User updatedUser = userService.updateUser(
                    id,
                    name.isEmpty() ? null : name,
                    email.isEmpty() ? null : email,
                    age
            );

            System.out.println("\nПользователь успешно обновлен!");
            printUserDetails(updatedUser);

        } catch (InputMismatchException e) {
            scanner.nextLine();
            System.out.println("\nID должен быть числом!");
        } catch (ValidationException e) {
            System.out.println("\nОшибка валидации: " + e.getMessage());
        } catch (UserNotFoundException e) {
            System.out.println("\n" + e.getMessage());
        } catch (UserAlreadyExistsException e) {
            System.out.println("\n " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nОшибка при обновлении: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        System.out.println("\n" + "─".repeat(50));
        System.out.println("            УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ");
        System.out.println("─".repeat(50));

        try {
            System.out.println("Введите ID пользователя для удаления: ");
            Long id = scanner.nextLong();
            scanner.nextLine();

            User user = userService.getUserById(id);
            System.out.println("\nПользователь для удаления:");
            printUserDetails(user);

            System.out.println("\nВы уверены, что хотите удалить этого пользователя? (да/нет): ");
            String confirmation = scanner.nextLine();

            if (confirmation.equalsIgnoreCase("да") || confirmation.equalsIgnoreCase("yes")) {
                userService.deleteUser(id);
                System.out.println("\nПользователь успешно удален!");
            } else {
                System.out.println("\nУдаление отменено.");
            }
        } catch (InputMismatchException e) {
            scanner.nextLine();
            System.out.println("\n ID должен быть числом!");
        } catch (UserNotFoundException e) {
            System.out.println("\n " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nОшибка при удалении: " + e.getMessage());
        }
    }

    private static void showUserCount() {
        System.out.println("\n" + "─".repeat(50));
        System.out.println("         СТАТИСТИКА ПОЛЬЗОВАТЕЛЕЙ");
        System.out.println("─".repeat(50));

        try {
            long count = userService.getUserCount();
            System.out.println("Всего пользователей в базе данных: \" + count");
        } catch (Exception e) {
            System.out.println("\nОшибка при получении статистики: " + e.getMessage());
        }
    }

    private static void deleteAllUsers() {
        System.out.println("\n" + "─".repeat(50));
        System.out.println("        !!УДАЛЕНИЕ ВСЕХ ПОЛЬЗОВАТЕЛЕЙ!! ");
        System.out.println("─".repeat(50));

        try {
            long count = userService.getUserCount();

            if (count == 0) {
                System.out.println("\nБаза данных уже пуста.");
                return;
            }

            System.out.println("\nВНИМАНИЕ! Эта операция удалит ВСЕ " + count + " пользователей!");
            System.out.print("Вы уверены? Введите 'УДАЛИТЬ ВСЁ' для подтверждения: ");
            String confirmation = scanner.nextLine();

            if (confirmation.equals("УДАЛИТЬ ВСЁ")) {
                userService.deleteAllUsers();
                System.out.println("\n Все пользователи успешно удалены!");
            } else {
                System.out.println("\nУдаление отменено.");
            }
        } catch (Exception e) {
            System.out.println("\nОшибка при удалении: " + e.getMessage());
        }
    }

    private static void printUserDetails(User user) {
        System.out.println("\n┌" + "─".repeat(48) + "┐");
        System.out.println("│ ID:         " + String.format("%-34s", user.getId()) + "│");
        System.out.println("│ Имя:        " + String.format("%-34s", user.getName()) + "│");
        System.out.println("│ Email:      " + String.format("%-34s", user.getEmail()) + "│");
        System.out.println("│ Возраст:    " + String.format("%-34s", user.getAge() != null ? user.getAge() : "не указан") + "│");
        System.out.println("│ Создан:     " + String.format("%-34s", user.getCreatedAt()) + "│");
        System.out.println("└" + "─".repeat(48) + "┘");
    }

    private static void printUsersTable(List<User> users) {
        System.out.println("┌" + "─".repeat(6) + "┬" + "─".repeat(25) + "┬" + "─".repeat(30) + "┬" + "─".repeat(10) + "┐");
        System.out.println("│ " + String.format("%-4s", "ID") +
                " │ " + String.format("%-23s", "Имя") +
                " │ " + String.format("%-28s", "Email") +
                " │ " + String.format("%-8s", "Возраст") + " │");
        System.out.println("├" + "─".repeat(6) + "┼" + "─".repeat(25) + "┼" + "─".repeat(30) + "┼" + "─".repeat(10) + "┤");

        for (User user : users){
            String id = String.valueOf(user.getId());
            String name = truncate(user.getName(), 23);
            String email = truncate(user.getEmail(), 28);
            String age = user.getAge() != null ? String.valueOf(user.getAge()) : "-";

            System.out.println("│ " + String.format("%-4s", id) +
                    " │ " + String.format("%-23s", name) +
                    " │ " + String.format("%-28s", email) +
                    " │ " + String.format("%-8s", age) + " │");
        }

        System.out.println("└" + "─".repeat(6) + "┴" + "─".repeat(25) + "┴" + "─".repeat(30) + "┴" + "─".repeat(10) + "┘");
    }

    private static String truncate(String str, int maxLength){
        if (str == null){
            return "";
        }
        if (str.length() <= maxLength){
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}