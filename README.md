# J-MAB Mobile

J-MAB Mobile is an Android application designed to help users get the most out of their vehicles by providing a wide range of high-quality car supplies, from performance parts to everyday essentials. The app supports shopping, order management, notifications, and real-time messaging, all built with a modern MVVM architecture.

---

## Features

- **Product Catalog**: Browse and search for car supplies by category and brand.
- **Shopping Cart**: Add products to your cart, update quantities, and proceed to checkout.
- **Order Management**: View all your orders, filter by status (To Pay, To Ship, To Receive, To Rate, Cancelled), and see order details.
- **User Account**: Manage your profile, addresses, and account security.
- **Notifications**: Receive real-time notifications about orders and promotions.
- **Messaging**: Chat with admins or support via real-time WebSocket messaging.
- **Authentication**: Secure sign-up, sign-in, and password reset flows.
- **MVVM Architecture**: Clean separation of concerns for maintainability and testability.

---

## Project Structure (MVVM)

```
app/
  └── src/
      └── main/
          ├── java/com/example/j_mabmobile/
          │   ├── model/         # Data models (User, Product, Order, etc.)
          │   ├── viewmodels/    # ViewModel classes (CartViewModel, OrdersViewModel, etc.)
          │   ├── api/           # API and network logic (Retrofit, WebSocket, etc.)
          │   ├── ...            # Activities, Fragments, Adapters (Views)
          └── res/
              ├── layout/        # XML layouts for Activities and Fragments
              └── ...            # Other resources (drawables, values, etc.)
```

- **Model**: `model/` and `api/` (data classes, API interfaces, network logic)
- **ViewModel**: `viewmodels/` (business logic, LiveData, state management)
- **View**: Activities, Fragments, Adapters, and XML layouts

---

## Getting Started

### Prerequisites

- Android Studio (latest recommended)
- Android SDK (API 24+)
- Java 8 or higher

### Setup

1. **Clone the repository**  
   ```
   git clone <your-repo-url>
   cd JMABMobile
   ```

2. **Open in Android Studio**  
   - Open the `JMABMobile` folder in Android Studio.

3. **Configure Backend/API**  
   - The app expects a backend server running at `http://10.0.2.2:80/JMAB/final-jmab/api/` (see `RetrofitClient.kt`).
   - WebSocket servers should be available at `ws://10.0.2.2:8080` (messages) and `ws://10.0.2.2:8081` (notifications).
   - Update these URLs in the code if your backend runs elsewhere.

4. **Build and Run**  
   - Connect an Android device or start an emulator.
   - Click "Run" in Android Studio or use:
     ```
     ./gradlew assembleDebug
     ```

---

## Dependencies

- Retrofit, OkHttp, Gson (networking)
- AndroidX, Material Components
- Lottie, Picasso, DotsIndicator, FlexboxLayout
- Lifecycle, ViewModel, LiveData
- ZXing (QR code), iText (PDF)

See `app/build.gradle.kts` for the full list.

---

## Permissions

The app requests the following permissions:
- Internet, Network State
- Camera, Storage (for profile and product images)
- Notifications (Android 13+)
- Foreground Service (for real-time updates)

---

## License

This project is for educational and demonstration purposes. Please contact the author for production/commercial use.

---

## About

At **J-MAB**, we’re passionate about helping you get the most out of your vehicle. We offer a wide range of high-quality car supplies, from performance parts to everyday essentials, all designed to keep your car running smoothly and efficiently. Your journey matters to us, and we’re here to support you every mile of the way with reliable, top-notch car care solutions. 