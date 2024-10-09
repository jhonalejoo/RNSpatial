#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(RNSpatial, NSObject)

RCT_EXTERN_METHOD(connect:(NSDictionary *)paramsDataBase withResolver:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(executeQuery:(NSString *)query withResolver:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
