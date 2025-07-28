# ClipSync Developer Guide

## Architecture Overview

ClipSync follows a service-oriented architecture designed for background clipboard synchronization:

### Core Components

1. **MainActivity**: Entry point for user interaction and service management
2. **BluetoothService**: Foreground service handling all Bluetooth operations
3. **Essentials**: Global state manager for service communication
4. **NotificationReceiver**: Broadcast receiver for notification actions

### Design Patterns

#### Service-Oriented Architecture

- **BluetoothService** runs independently as a foreground service
- Survives Activity lifecycle for continuous background operation
- Communicates via the Essentials singleton for real-time updates

#### Global State Management (Essentials Pattern)

While typically considered an anti-pattern, the Essentials singleton is justified here because:

- Service needs to persist beyond Activity lifecycle
- Multiple components require real-time service state access
- Avoids service restart overhead for configuration updates
- Properly cleaned up when service stops

### Key Features

- **Background Operation**: Service continues running when app is closed
- **Notification Control**: Start/stop service via persistent notification
- **Real-time Updates**: Update service configuration without restart
- **Thread Safety**: Mutex-protected state management

## Development Setup

### Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK 24+ (Android 7.0)
- Kotlin 1.8+
- Gradle 8.0+

### Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Code Style Guidelines

### Kotlin Conventions

- Follow official Kotlin coding conventions
- Use meaningful variable names
- Document public APIs with KDoc
- Prefer immutable data structures

### Architecture Guidelines

- Keep Activities lightweight - delegate to services
- Use coroutines for async operations
- Implement proper error handling
- Follow single responsibility principle

## Security Considerations

### Data Validation

- Validate clipboard content size (max 1MB)
- Check for potentially sensitive data patterns
- Sanitize device names for display

### Bluetooth Security

- Use secure RFCOMM connections
- Validate device addresses
- Implement connection timeouts

### Privacy

- No cloud storage - all data local
- Temporary clipboard storage only
- Clear sensitive data on service stop

## Testing Strategy

### Unit Tests

- Core business logic
- Data validation
- State management

### Integration Tests

- Bluetooth service operations
- Notification handling
- Permission management

### Manual Testing Checklist

- [ ] Service starts/stops correctly
- [ ] Clipboard sync between devices
- [ ] Notification actions work
- [ ] Permission handling
- [ ] Battery optimization compatibility

## Performance Optimizations

### Memory Management

- Use weak references where appropriate
- Clean up resources in onDestroy()
- Avoid memory leaks in long-running service

### Battery Optimization

- Efficient Bluetooth operations
- Minimize wake locks
- Use appropriate service types

### Network Efficiency

- Connection pooling for multiple devices
- Retry logic with exponential backoff
- Timeout handling

## Debugging Tips

### Logging

- Use structured logging with tags
- Different log levels for debug/release
- Include relevant context in logs

### Common Issues

1. **Service not starting**: Check permissions and Bluetooth state
2. **Devices not connecting**: Verify pairing and RFCOMM setup
3. **Memory leaks**: Use LeakCanary for detection
4. **Battery drain**: Profile with Android Studio

## Release Process

### Version Management

- Update versionCode and versionName in build.gradle.kts
- Follow semantic versioning (MAJOR.MINOR.PATCH)
- Update CHANGELOG.md

### Build Configuration

- Use release build type for production
- Enable ProGuard/R8 optimization
- Test thoroughly before release

### GitHub Release Steps

1. Create release branch
2. Update version numbers
3. Run full test suite
4. Build signed APK
5. Create GitHub release with changelog
6. Upload APK to release

## Contributing

### Code Review Checklist

- [ ] Code follows style guidelines
- [ ] Tests added for new features
- [ ] Documentation updated
- [ ] No breaking changes without migration
- [ ] Performance impact considered

### Pull Request Template

- Clear description of changes
- Link to related issues
- Screenshots for UI changes
- Test results included

## Troubleshooting

### Build Issues

- Clean and rebuild project
- Invalidate caches and restart
- Check Gradle and SDK versions

### Runtime Issues

- Check device logs with `adb logcat`
- Verify permissions in device settings
- Test on different Android versions

## Future Enhancements

### Planned Features

- File sharing support
- Encryption for sensitive data
- Multi-device group management
- Cross-platform compatibility

### Technical Debt

- Migrate to Bluetooth LE for better efficiency
- Implement proper dependency injection
- Add comprehensive error recovery
- Improve test coverage
