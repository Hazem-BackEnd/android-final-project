# RegisterViewModel Unit Tests

## Test Coverage Summary

### ✅ All Tests Passing (25+ tests total)

## Test Files

### 1. RegisterViewModelTest.kt - Basic Functionality (8 tests)
- ✅ `signUp with valid data without image should return success state`
- ✅ `signUp with valid data and image should upload image then register`
- ✅ `signUp with invalid data should return error state`
- ✅ `signUp with repository exception should return error state`
- ✅ `initial state should be Nothing`
- ✅ `signUp with image upload failure should still proceed with registration`
- ✅ `signUp with empty fields should still call repository`
- ✅ `multiple signUp calls should work independently`

### 2. RegisterViewModelAdvancedTest.kt - State Flow & Async (7 tests)
- ✅ `signUp should emit loading then success states in correct order`
- ✅ `signUp should emit loading then error states when registration fails`
- ✅ `signUp with image should handle image upload then registration sequence`
- ✅ `signUp should handle storage repository exception and emit error state`
- ✅ `signUp with successful image upload but failed registration should emit error`
- ✅ `multiple signUp calls should eventually reach final state`
- ✅ `signUp with failed image upload should proceed with null profile picture`

### 3. RegisterViewModelEdgeCasesTest.kt - Edge Cases & Integration (12 tests)
- ✅ `signUp with very long input fields should work`
- ✅ `signUp with special characters should work`
- ✅ `signUp with unicode characters should work`
- ✅ `different exception types should all result in error state`
- ✅ `signUp after previous error should reset state correctly`
- ✅ `signUp after previous success should reset state correctly`
- ✅ `signUp with whitespace-only fields should be handled`
- ✅ `viewModel should handle rapid successive calls`
- ✅ `signUp with image upload exception should handle gracefully`
- ✅ `signUp with null fields should still call repository`
- ✅ `signUp with mixed success and failure scenarios should work correctly`

## Key Test Scenarios Covered

### ✅ **Registration Flow**
- **Without Image**: Direct registration with AuthRepository
- **With Image**: Image upload → Registration sequence
- **Image Upload Failure**: Graceful fallback to registration without image
- **Registration Failure**: Proper error handling after successful image upload

### ✅ **State Management**
- **Initial State**: Nothing state on ViewModel creation
- **Loading State**: Proper loading state during async operations
- **Success State**: Successful registration completion
- **Error State**: Various error scenarios and recovery
- **State Transitions**: Proper state resets between calls

### ✅ **Input Validation & Edge Cases**
- **Empty Fields**: Handles empty/null inputs
- **Long Inputs**: Very long strings (1000+ characters)
- **Special Characters**: Names with accents, symbols, etc.
- **Unicode Support**: International characters (Chinese, Arabic, Cyrillic)
- **Whitespace**: Whitespace-only inputs

### ✅ **Error Handling**
- **Repository Exceptions**: AuthRepository failures
- **Storage Exceptions**: Image upload failures
- **Network Errors**: Simulated connection issues
- **Multiple Exception Types**: Different exception scenarios
- **Graceful Degradation**: Continues registration even if image upload fails

### ✅ **Async Behavior**
- **Coroutine Testing**: Proper async operation testing
- **State Flow Emissions**: Sequential state changes (Nothing → Loading → Success/Error)
- **Concurrent Calls**: Multiple simultaneous registration attempts
- **Timing**: Proper handling of async delays and operations

### ✅ **Integration Testing**
- **AuthRepository Integration**: Mocked repository interactions
- **StorageRepository Integration**: Mocked image upload service
- **Dependency Injection**: Proper Hilt ViewModel testing
- **Real-world Scenarios**: Mixed success/failure combinations

## Test Technologies Used

- **MockK**: For mocking AuthRepository and StorageRepository
- **Coroutines Test**: For testing async behavior with proper dispatchers
- **Turbine**: For testing StateFlow emissions and sequences
- **JUnit 4**: Test framework with proper rules
- **InstantTaskExecutorRule**: For LiveData/StateFlow testing
- **Android Uri Mocking**: For testing image upload scenarios

## Running Tests

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run with continue flag to see all results
./gradlew testDebugUnitTest --continue

# Clean and run tests
./gradlew clean testDebugUnitTest
```

## Test Architecture

### **Mocking Strategy**
- **AuthRepository**: Mocked to return success/failure results
- **StorageRepository**: Mocked to simulate image upload scenarios
- **Android Uri**: Mocked for image selection testing
- **Context**: Not needed due to mocking approach

### **State Flow Testing**
- **Turbine Library**: Used for testing StateFlow emissions
- **Test Dispatchers**: Proper coroutine testing with StandardTestDispatcher
- **State Sequences**: Verification of correct state transitions

### **Edge Case Coverage**
- **Boundary Testing**: Empty, null, very long inputs
- **Character Encoding**: Unicode and special character support
- **Error Recovery**: State resets and multiple call scenarios
- **Performance**: Rapid successive calls and timing

## Key Differences from LoginViewModel Tests

### **Additional Complexity**
- **Image Upload Flow**: Tests image upload before registration
- **Two-Step Process**: Image upload → Registration sequence
- **Graceful Degradation**: Continues without image if upload fails
- **More Parameters**: Full name, phone number, email, password, image

### **Enhanced Error Scenarios**
- **Storage Failures**: Image upload specific errors
- **Mixed Results**: Image success + registration failure combinations
- **Resource Management**: Proper handling of Uri and streams

### **Real-world Scenarios**
- **Profile Pictures**: Complete user registration with images
- **International Users**: Unicode name and contact support
- **Network Resilience**: Handles partial failures gracefully

The RegisterViewModel tests provide comprehensive coverage ensuring the registration flow works correctly with all edge cases, error scenarios, and real-world usage patterns.