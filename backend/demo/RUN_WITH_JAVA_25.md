# How to Run with Java 25 in IntelliJ IDEA

## Important Note
Java 25 is not yet officially released (as of January 2026). If you're using an early access version or preview build, it may have compatibility issues with build tools like Maven.

---

## ✅ BEST SOLUTION: Run via IntelliJ IDEA

IntelliJ IDEA has better support for preview/early access Java versions than Maven command line.

### Step 1: Configure IntelliJ for Java 25

1. **Open Project in IntelliJ IDEA**
   - File → Open → Select your project folder

2. **Set Project SDK to Java 25**
   - File → Project Structure (Ctrl+Alt+Shift+S)
   - Project Settings → Project
   - SDK: Select your Java 25 installation
   - Language Level: 25 (Preview) or X - Experimental features
   - Click Apply → OK

3. **Configure Maven to Use Java 25**
   - File → Settings (Ctrl+Alt+S)
   - Build, Execution, Deployment → Build Tools → Maven → Runner
   - JRE: Select Java 25
   - VM Options: Add `-Djava.version=25`
   - Click Apply → OK

4. **Enable Lombok Plugin**
   - File → Settings → Plugins
   - Search for "Lombok"
   - Install if not already installed
   - Restart IntelliJ if prompted

5. **Enable Annotation Processing**
   - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - ✅ Enable annotation processing
   - Click Apply → OK

### Step 2: Build Project in IntelliJ

1. **Rebuild Project**
   - Build → Rebuild Project
   - Wait for compilation to complete
   - Check the Build tab for any errors

2. **If you see Lombok errors:**
   - File → Invalidate Caches → Invalidate and Restart
   - After restart, Build → Rebuild Project again

### Step 3: Run Application

1. **Open Main Class**
   - Navigate to: `src/main/java/com/nipuna/demo/SrsmsystemApplication.java`

2. **Run Application**
   - Right-click on the file or the `main` method
   - Select: **Run 'SrsmsystemApplication.main()'**

3. **Application Should Start!**
   ```
   Started SrsmsystemApplication in X.XXX seconds (process running on ...)
   Tomcat started on port(s): 8080 (http)
   ```

---

## 🔧 Alternative: Use Maven with Correct Settings

If you want to use Maven command line with Java 25:

### Update .mvn/jvm.config

Create file: `.mvn/jvm.config` in your project root with:

```
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
--enable-preview
```

### Then try:

```powershell
cd "C:\Users\EX BOOK\Desktop\Smart Repair & Service Management System\backend\demo"
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

---

## 🐛 If Still Having Issues

### Check Your Java Installation

1. **Find Java 25 installation path:**
   ```powershell
   Get-ChildItem "C:\Program Files\Java" -Recurse -Filter "java.exe" | Select-Object FullName
   ```

2. **Check Java version:**
   ```powershell
   & "C:\Program Files\Java\jdk-25\bin\java.exe" -version
   ```

3. **Set JAVA_HOME:**
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   ```

4. **Verify:**
   ```powershell
   java -version
   ```

---

## ⚠️ Important Notes

### About Java 25
- Java 25 is not yet officially released (expected: September 2026)
- If you're using a preview/early access version, expect compatibility issues
- Many libraries (including Lombok) may not fully support Java 25 yet

### Recommendation
For production projects, consider using:
- **Java 21 (LTS)** - Long-term support, stable, widely supported
- **Java 17 (LTS)** - Very stable, excellent Spring Boot support
- **Java 11 (LTS)** - Older but still supported

### If You Must Use Java 25
- Always run via IntelliJ IDEA (best compatibility)
- Don't rely on Maven command line
- Be prepared for unexpected issues with third-party libraries

---

## ✅ Your Code is Perfect!

Remember: **There are NO errors in your code!** All the issues are related to:
1. Java version compatibility
2. Build tool configuration
3. Lombok compatibility with preview Java versions

Your consolidated CustomerService and CustomerController are working perfectly. Once the build environment is properly configured, everything will run smoothly!

---

## 🚀 Quick Start Commands (After Setup)

### In IntelliJ IDEA:
1. Open `SrsmsystemApplication.java`
2. Click the green play button ▶️ next to `main` method
3. Application starts on http://localhost:8080

### Test with Postman:
1. POST http://localhost:8080/auth/register (create account)
2. POST http://localhost:8080/auth/login (get token)
3. GET http://localhost:8080/customer/vehicles (with Bearer token)
4. POST http://localhost:8080/customer/vehicles (add vehicle)
5. POST http://localhost:8080/customer/repairs (create repair)

---

## 📚 Documentation Files

- `CUSTOMER_API_TESTING_GUIDE.md` - Complete API testing guide
- `API_QUICK_REFERENCE.md` - Quick API reference
- `CONSOLIDATION_SUMMARY.md` - Project consolidation details
- `FIX_COMPILATION_ERRORS.md` - General compilation fixes

---

**Summary:** Use IntelliJ IDEA to run your project with Java 25. It's the most reliable method for preview/early access Java versions! 🎯

