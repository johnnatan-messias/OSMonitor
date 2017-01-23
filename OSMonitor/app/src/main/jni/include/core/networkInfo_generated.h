// automatically generated by the FlatBuffers compiler, do not modify

#ifndef FLATBUFFERS_GENERATED_NETWORKINFO_COM_EOLWRAL_OSMONITOR_CORE_H_
#define FLATBUFFERS_GENERATED_NETWORKINFO_COM_EOLWRAL_OSMONITOR_CORE_H_

#include "flatbuffers/flatbuffers.h"


namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

struct networkInfo;
struct networkInfoList;

struct networkInfo : private flatbuffers::Table {
  /// interface name 
  const flatbuffers::String *name() const { return GetPointer<const flatbuffers::String *>(4); }
  /// MAC address 
  const flatbuffers::String *mac() const { return GetPointer<const flatbuffers::String *>(6); }
  /// IPv4 address 
  const flatbuffers::String *ipv4Addr() const { return GetPointer<const flatbuffers::String *>(8); }
  /// IPv4 netmask 
  const flatbuffers::String *netMaskv4() const { return GetPointer<const flatbuffers::String *>(10); }
  /// IPv6 address 
  const flatbuffers::String *ipv6Addr() const { return GetPointer<const flatbuffers::String *>(12); }
  /// IPv6 netmask 
  uint32_t netMaskv6() const { return GetField<uint32_t>(14, 0); }
  /// status flag 
  uint32_t flags() const { return GetField<uint32_t>(16, 0); }
  /// received bytes 
  uint64_t recvBytes() const { return GetField<uint64_t>(18, 0); }
  /// received packages 
  uint64_t recvPackages() const { return GetField<uint64_t>(20, 0); }
  /// error bytes when receiving 
  uint64_t recvErrorBytes() const { return GetField<uint64_t>(22, 0); }
  /// drop bytes when receiving  
  uint64_t recvDropBytes() const { return GetField<uint64_t>(24, 0); }
  /// FIFO bytes when receiving 
  uint64_t recvFIFOBytes() const { return GetField<uint64_t>(26, 0); }
  /// received frames 
  uint64_t recvFrames() const { return GetField<uint64_t>(28, 0); }
  /// received compressed bytes 
  uint64_t recvCompressedBytes() const { return GetField<uint64_t>(30, 0); }
  /// received multi-cast bytes 
  uint64_t recvMultiCastBytes() const { return GetField<uint64_t>(32, 0); }
  /// transmitted bytes 
  uint64_t transBytes() const { return GetField<uint64_t>(34, 0); }
  /// transmitted packages 
  uint64_t transPackages() const { return GetField<uint64_t>(36, 0); }
  /// error bytes when transmitting 
  uint64_t transErrorBytes() const { return GetField<uint64_t>(38, 0); }
  /// dropped bytes when transmitting 
  uint64_t transDropBytes() const { return GetField<uint64_t>(40, 0); }
  /// FIFO bytes when transmitting 
  uint64_t transFIFOBytes() const { return GetField<uint64_t>(42, 0); }
  /// transmitted compressed bytes 
  uint64_t transCompressedBytes() const { return GetField<uint64_t>(44, 0); }
  /// collision times 
  uint32_t collisionTimes() const { return GetField<uint32_t>(46, 0); }
  /// carrier error times 
  uint32_t carrierErros() const { return GetField<uint32_t>(48, 0); }
  /// transmitted usage 
  uint64_t transUsage() const { return GetField<uint64_t>(50, 0); }
  /// received usage 
  uint64_t recvUsage() const { return GetField<uint64_t>(52, 0); }
  bool Verify(flatbuffers::Verifier &verifier) const {
    return VerifyTableStart(verifier) &&
           VerifyField<flatbuffers::uoffset_t>(verifier, 4 /* name */) &&
           verifier.Verify(name()) &&
           VerifyField<flatbuffers::uoffset_t>(verifier, 6 /* mac */) &&
           verifier.Verify(mac()) &&
           VerifyField<flatbuffers::uoffset_t>(verifier, 8 /* ipv4Addr */) &&
           verifier.Verify(ipv4Addr()) &&
           VerifyField<flatbuffers::uoffset_t>(verifier, 10 /* netMaskv4 */) &&
           verifier.Verify(netMaskv4()) &&
           VerifyField<flatbuffers::uoffset_t>(verifier, 12 /* ipv6Addr */) &&
           verifier.Verify(ipv6Addr()) &&
           VerifyField<uint32_t>(verifier, 14 /* netMaskv6 */) &&
           VerifyField<uint32_t>(verifier, 16 /* flags */) &&
           VerifyField<uint64_t>(verifier, 18 /* recvBytes */) &&
           VerifyField<uint64_t>(verifier, 20 /* recvPackages */) &&
           VerifyField<uint64_t>(verifier, 22 /* recvErrorBytes */) &&
           VerifyField<uint64_t>(verifier, 24 /* recvDropBytes */) &&
           VerifyField<uint64_t>(verifier, 26 /* recvFIFOBytes */) &&
           VerifyField<uint64_t>(verifier, 28 /* recvFrames */) &&
           VerifyField<uint64_t>(verifier, 30 /* recvCompressedBytes */) &&
           VerifyField<uint64_t>(verifier, 32 /* recvMultiCastBytes */) &&
           VerifyField<uint64_t>(verifier, 34 /* transBytes */) &&
           VerifyField<uint64_t>(verifier, 36 /* transPackages */) &&
           VerifyField<uint64_t>(verifier, 38 /* transErrorBytes */) &&
           VerifyField<uint64_t>(verifier, 40 /* transDropBytes */) &&
           VerifyField<uint64_t>(verifier, 42 /* transFIFOBytes */) &&
           VerifyField<uint64_t>(verifier, 44 /* transCompressedBytes */) &&
           VerifyField<uint32_t>(verifier, 46 /* collisionTimes */) &&
           VerifyField<uint32_t>(verifier, 48 /* carrierErros */) &&
           VerifyField<uint64_t>(verifier, 50 /* transUsage */) &&
           VerifyField<uint64_t>(verifier, 52 /* recvUsage */) &&
           verifier.EndTable();
  }
};

struct networkInfoBuilder {
  flatbuffers::FlatBufferBuilder &fbb_;
  flatbuffers::uoffset_t start_;
  void add_name(flatbuffers::Offset<flatbuffers::String> name) { fbb_.AddOffset(4, name); }
  void add_mac(flatbuffers::Offset<flatbuffers::String> mac) { fbb_.AddOffset(6, mac); }
  void add_ipv4Addr(flatbuffers::Offset<flatbuffers::String> ipv4Addr) { fbb_.AddOffset(8, ipv4Addr); }
  void add_netMaskv4(flatbuffers::Offset<flatbuffers::String> netMaskv4) { fbb_.AddOffset(10, netMaskv4); }
  void add_ipv6Addr(flatbuffers::Offset<flatbuffers::String> ipv6Addr) { fbb_.AddOffset(12, ipv6Addr); }
  void add_netMaskv6(uint32_t netMaskv6) { fbb_.AddElement<uint32_t>(14, netMaskv6, 0); }
  void add_flags(uint32_t flags) { fbb_.AddElement<uint32_t>(16, flags, 0); }
  void add_recvBytes(uint64_t recvBytes) { fbb_.AddElement<uint64_t>(18, recvBytes, 0); }
  void add_recvPackages(uint64_t recvPackages) { fbb_.AddElement<uint64_t>(20, recvPackages, 0); }
  void add_recvErrorBytes(uint64_t recvErrorBytes) { fbb_.AddElement<uint64_t>(22, recvErrorBytes, 0); }
  void add_recvDropBytes(uint64_t recvDropBytes) { fbb_.AddElement<uint64_t>(24, recvDropBytes, 0); }
  void add_recvFIFOBytes(uint64_t recvFIFOBytes) { fbb_.AddElement<uint64_t>(26, recvFIFOBytes, 0); }
  void add_recvFrames(uint64_t recvFrames) { fbb_.AddElement<uint64_t>(28, recvFrames, 0); }
  void add_recvCompressedBytes(uint64_t recvCompressedBytes) { fbb_.AddElement<uint64_t>(30, recvCompressedBytes, 0); }
  void add_recvMultiCastBytes(uint64_t recvMultiCastBytes) { fbb_.AddElement<uint64_t>(32, recvMultiCastBytes, 0); }
  void add_transBytes(uint64_t transBytes) { fbb_.AddElement<uint64_t>(34, transBytes, 0); }
  void add_transPackages(uint64_t transPackages) { fbb_.AddElement<uint64_t>(36, transPackages, 0); }
  void add_transErrorBytes(uint64_t transErrorBytes) { fbb_.AddElement<uint64_t>(38, transErrorBytes, 0); }
  void add_transDropBytes(uint64_t transDropBytes) { fbb_.AddElement<uint64_t>(40, transDropBytes, 0); }
  void add_transFIFOBytes(uint64_t transFIFOBytes) { fbb_.AddElement<uint64_t>(42, transFIFOBytes, 0); }
  void add_transCompressedBytes(uint64_t transCompressedBytes) { fbb_.AddElement<uint64_t>(44, transCompressedBytes, 0); }
  void add_collisionTimes(uint32_t collisionTimes) { fbb_.AddElement<uint32_t>(46, collisionTimes, 0); }
  void add_carrierErros(uint32_t carrierErros) { fbb_.AddElement<uint32_t>(48, carrierErros, 0); }
  void add_transUsage(uint64_t transUsage) { fbb_.AddElement<uint64_t>(50, transUsage, 0); }
  void add_recvUsage(uint64_t recvUsage) { fbb_.AddElement<uint64_t>(52, recvUsage, 0); }
  networkInfoBuilder(flatbuffers::FlatBufferBuilder &_fbb) : fbb_(_fbb) { start_ = fbb_.StartTable(); }
  networkInfoBuilder &operator=(const networkInfoBuilder &);
  flatbuffers::Offset<networkInfo> Finish() {
    auto o = flatbuffers::Offset<networkInfo>(fbb_.EndTable(start_, 25));
    return o;
  }
};

inline flatbuffers::Offset<networkInfo> CreatenetworkInfo(flatbuffers::FlatBufferBuilder &_fbb,
   flatbuffers::Offset<flatbuffers::String> name = 0,
   flatbuffers::Offset<flatbuffers::String> mac = 0,
   flatbuffers::Offset<flatbuffers::String> ipv4Addr = 0,
   flatbuffers::Offset<flatbuffers::String> netMaskv4 = 0,
   flatbuffers::Offset<flatbuffers::String> ipv6Addr = 0,
   uint32_t netMaskv6 = 0,
   uint32_t flags = 0,
   uint64_t recvBytes = 0,
   uint64_t recvPackages = 0,
   uint64_t recvErrorBytes = 0,
   uint64_t recvDropBytes = 0,
   uint64_t recvFIFOBytes = 0,
   uint64_t recvFrames = 0,
   uint64_t recvCompressedBytes = 0,
   uint64_t recvMultiCastBytes = 0,
   uint64_t transBytes = 0,
   uint64_t transPackages = 0,
   uint64_t transErrorBytes = 0,
   uint64_t transDropBytes = 0,
   uint64_t transFIFOBytes = 0,
   uint64_t transCompressedBytes = 0,
   uint32_t collisionTimes = 0,
   uint32_t carrierErros = 0,
   uint64_t transUsage = 0,
   uint64_t recvUsage = 0) {
  networkInfoBuilder builder_(_fbb);
  builder_.add_recvUsage(recvUsage);
  builder_.add_transUsage(transUsage);
  builder_.add_transCompressedBytes(transCompressedBytes);
  builder_.add_transFIFOBytes(transFIFOBytes);
  builder_.add_transDropBytes(transDropBytes);
  builder_.add_transErrorBytes(transErrorBytes);
  builder_.add_transPackages(transPackages);
  builder_.add_transBytes(transBytes);
  builder_.add_recvMultiCastBytes(recvMultiCastBytes);
  builder_.add_recvCompressedBytes(recvCompressedBytes);
  builder_.add_recvFrames(recvFrames);
  builder_.add_recvFIFOBytes(recvFIFOBytes);
  builder_.add_recvDropBytes(recvDropBytes);
  builder_.add_recvErrorBytes(recvErrorBytes);
  builder_.add_recvPackages(recvPackages);
  builder_.add_recvBytes(recvBytes);
  builder_.add_carrierErros(carrierErros);
  builder_.add_collisionTimes(collisionTimes);
  builder_.add_flags(flags);
  builder_.add_netMaskv6(netMaskv6);
  builder_.add_ipv6Addr(ipv6Addr);
  builder_.add_netMaskv4(netMaskv4);
  builder_.add_ipv4Addr(ipv4Addr);
  builder_.add_mac(mac);
  builder_.add_name(name);
  return builder_.Finish();
}

struct networkInfoList : private flatbuffers::Table {
  const flatbuffers::Vector<flatbuffers::Offset<networkInfo>> *list() const { return GetPointer<const flatbuffers::Vector<flatbuffers::Offset<networkInfo>> *>(4); }
  bool Verify(flatbuffers::Verifier &verifier) const {
    return VerifyTableStart(verifier) &&
           VerifyField<flatbuffers::uoffset_t>(verifier, 4 /* list */) &&
           verifier.Verify(list()) &&
           verifier.VerifyVectorOfTables(list()) &&
           verifier.EndTable();
  }
};

struct networkInfoListBuilder {
  flatbuffers::FlatBufferBuilder &fbb_;
  flatbuffers::uoffset_t start_;
  void add_list(flatbuffers::Offset<flatbuffers::Vector<flatbuffers::Offset<networkInfo>>> list) { fbb_.AddOffset(4, list); }
  networkInfoListBuilder(flatbuffers::FlatBufferBuilder &_fbb) : fbb_(_fbb) { start_ = fbb_.StartTable(); }
  networkInfoListBuilder &operator=(const networkInfoListBuilder &);
  flatbuffers::Offset<networkInfoList> Finish() {
    auto o = flatbuffers::Offset<networkInfoList>(fbb_.EndTable(start_, 1));
    return o;
  }
};

inline flatbuffers::Offset<networkInfoList> CreatenetworkInfoList(flatbuffers::FlatBufferBuilder &_fbb,
   flatbuffers::Offset<flatbuffers::Vector<flatbuffers::Offset<networkInfo>>> list = 0) {
  networkInfoListBuilder builder_(_fbb);
  builder_.add_list(list);
  return builder_.Finish();
}

inline const networkInfoList *GetnetworkInfoList(const void *buf) { return flatbuffers::GetRoot<networkInfoList>(buf); }

inline bool VerifynetworkInfoListBuffer(flatbuffers::Verifier &verifier) { return verifier.VerifyBuffer<networkInfoList>(); }

inline void FinishnetworkInfoListBuffer(flatbuffers::FlatBufferBuilder &fbb, flatbuffers::Offset<networkInfoList> root) { fbb.Finish(root); }

}  // namespace core
}  // namespace osmonitor
}  // namespace eolwral
}  // namespace com

#endif  // FLATBUFFERS_GENERATED_NETWORKINFO_COM_EOLWRAL_OSMONITOR_CORE_H_
