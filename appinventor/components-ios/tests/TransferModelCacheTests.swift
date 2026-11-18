// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import CryptoKit
import Foundation
import XCTest
@testable import AIComponentKit

/**
 * Tests for `TransferModelCache`, which works out where transfer models (for example MobileNet)
 * are published and keeps downloaded copies on disk. None of these tests use the network.
 */
class TransferModelCacheTests: XCTestCase {
  private let v1URL = "https://storage.googleapis.com/tfjs-models/tfjs/mobilenet_v1_0.25_224"
  private let v3URL = "https://www.kaggle.com/models/google/mobilenet-v3/TfJs/" +
      "small-100-224-feature-vector/1"

  private var written = [URL]()

  override func tearDown() {
    for url in written {
      try? FileManager.default.removeItem(at: url.deletingLastPathComponent())
    }
    written.removeAll()
    super.tearDown()
  }

  // MARK: resolve

  func testResolveKnownV1ByName() {
    let source = TransferModelCache.resolve(name: "mobilenet", url: nil)
    XCTAssertEqual(v1URL, source?.baseURL)
    XCTAssertEqual(false, source?.fileFormatQuery)
  }

  func testResolveKnownV3ByNameNeedsTheFileFormatQuery() {
    let source = TransferModelCache.resolve(name: "mobilenet_v3_small", url: "")
    XCTAssertEqual(v3URL, source?.baseURL)
    XCTAssertEqual(true, source?.fileFormatQuery)
  }

  func testResolveKnownNamesIgnoreCase() {
    XCTAssertEqual(v3URL, TransferModelCache.resolve(name: "MobileNet_V3_Small", url: nil)?.baseURL)
  }

  func testResolveUsesRecordedUrlOverName() {
    let source = TransferModelCache.resolve(name: "mobilenet",
                                            url: "https://models.test/custom/model.json")
    XCTAssertEqual("https://models.test/custom", source?.baseURL)
    XCTAssertEqual(false, source?.fileFormatQuery)
  }

  func testResolveStripsModelJsonQueryAndTrailingSlashes() {
    XCTAssertEqual("https://models.test/a/b",
                   TransferModelCache.resolve(name: "x",
                                              url: "https://models.test/a/b/model.json?x=1")?.baseURL)
    XCTAssertEqual("https://models.test/a/b",
                   TransferModelCache.resolve(name: "x", url: "https://models.test/a/b///")?.baseURL)
  }

  func testResolveRecognizesTFHubHosts() {
    XCTAssertEqual(true, TransferModelCache.resolve(name: "x",
        url: "https://tfhub.dev/google/model/1")?.fileFormatQuery)
  }

  func testResolveIgnoresNonHttpsUrlAndFallsBackToName() {
    // Models exported from a local development server record a localhost URL.
    let source = TransferModelCache.resolve(name: "mobilenet_v3_small",
                                            url: "http://localhost:5000/mobilenet_v3/model.json")
    XCTAssertEqual(v3URL, source?.baseURL)
  }

  func testResolveUnknownModelReturnsNil() {
    XCTAssertNil(TransferModelCache.resolve(name: "not_a_model", url: nil))
    XCTAssertNil(TransferModelCache.resolve(name: "not_a_model", url: "http://localhost/model.json"))
    XCTAssertNil(TransferModelCache.resolve(name: nil, url: nil))
  }

  // MARK: fetch

  func testFetchReturnsCachedFileWithoutDownloading() throws {
    let source = try XCTUnwrap(TransferModelCache.resolve(name: "x",
                                                          url: "https://models.test/cached/model.json"))
    try writeCached(source, "group1-shard1of2.bin", "weights")

    // The host does not exist, so this only succeeds if the cached copy is used.
    XCTAssertEqual("weights", try fetch(source, "group1-shard1of2.bin"))
  }

  func testFetchKeepsModelsApartByUrl() throws {
    let a = try XCTUnwrap(TransferModelCache.resolve(name: "x", url: "https://models.test/a"))
    let b = try XCTUnwrap(TransferModelCache.resolve(name: "x", url: "https://models.test/b"))
    try writeCached(a, "model.json", "model a")
    try writeCached(b, "model.json", "model b")

    XCTAssertEqual("model a", try fetch(a, "model.json"))
    XCTAssertEqual("model b", try fetch(b, "model.json"))
  }

  func testFetchRejectsInvalidPaths() throws {
    let source = try XCTUnwrap(TransferModelCache.resolve(name: "x", url: "https://models.test/m"))
    let invalid = ["", "../model.json", "a/../../b", "/model.json", "a//b", "model.json?x=1",
                   "a b", "model.json/", "%2e%2e/model.json"]
    for path in invalid {
      XCTAssertThrowsError(try fetch(source, path), "Expected '\(path)' to be rejected") { error in
        guard case TransferModelCache.CacheError.invalidPath = error else {
          return XCTFail("Unexpected error for '\(path)': \(error)")
        }
      }
    }
  }

  // MARK: Helpers

  private func cacheFile(_ source: TransferModelCache.Source, _ path: String) -> URL {
    let caches = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask)[0]
    let digest = Insecure.SHA1.hash(data: Data(source.baseURL.utf8))
        .map { String(format: "%02x", $0) }.joined()
    return caches.appendingPathComponent("transfer-models", isDirectory: true)
        .appendingPathComponent(digest, isDirectory: true).appendingPathComponent(path)
  }

  private func writeCached(_ source: TransferModelCache.Source, _ path: String,
                           _ contents: String) throws {
    let url = cacheFile(source, path)
    try FileManager.default.createDirectory(at: url.deletingLastPathComponent(),
                                            withIntermediateDirectories: true)
    try Data(contents.utf8).write(to: url)
    written.append(url)
  }

  private func fetch(_ source: TransferModelCache.Source, _ path: String) throws -> String {
    let done = expectation(description: "fetch \(path)")
    var outcome: Result<Data, Error>?
    TransferModelCache.shared.fetch(source, path: path) { result in
      outcome = result
      done.fulfill()
    }
    wait(for: [done], timeout: 10)
    let data = try XCTUnwrap(outcome).get()
    return String(decoding: data, as: UTF8.self)
  }
}
