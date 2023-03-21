# <img align="center" width="100" height="100" src="https://www.logunify.com/favicon.ico"> LogUnify

## What is LogUnify

LogUnify is a schema-centric service that provides structured application event logging and
seamless integration with data warehouses such as BigQuery for easy storage and analysis of event data.

[Demo video](https://www.youtube.com/embed/NNepeRSNjqE ":include :type=iframe width=50% height=600px")

## Why LogUnify?

LogUnify is built around schemas based on protobuf and all the events in LogUnify are structured. By schematizing events, we offer the following benefits:

- **Consistency:** All the events across all platforms and languages are produced in a uniform way with the generated type-safe SDKs, which ensures data
  consistency from the root.
- **Shared Understanding:** Event schemas with rich data types and metadata are self-explained, which enables different departments from developers to business
  stakeholders to share the same understanding when consuming the events.
- **Flexibility:** LogUnify offers flexibility in terms of integrating with different systems, as the schemas can be easily converted into different formats and
  protocols without requiring dedicated transformations from developers. The platform currently offers seamless integration with BigQuery and is expanding to
  support other systems.
- **Reliability:** Structured events are more reliable as they are logged in the desired format from the start by developers who have full control over the
  producing logic. While traditional solutions require developers to log events in an unstructured manner and then set up the transformation on the server side.
  Data Analysis: Structured events enable better and more accurate data analysis naturally, as they provide a standardized and consistent format that can be
  easily processed and analyzed using various data analysis tools and techniques.

## Key Features

- **Structured Event Schema Definition:** YAML-based event schema definition with support for rich data types and metadata.
- **Codegen:** Code generation from event schema to create strongly-typed event definitions that are ready in multiple languages, such as Java, Swift, and
  TypeScript.
- **Client SDK:** Client SDKs of multiple platforms that are seamlessly integrated with the generated events definition to streamline the integration process on
  event producing and consuming\* _[On roadmap]_. Currently we support Android, iOS and Node.
- **Event Sinks:** Event sinks dictate the destinations for events and a single schema can be connected to multiple event sinks. With the schema, event sinks
  can automatically create tables, define schema, and handle schema evolution without requiring developers to create dedicated transformations.
- _[On roadmap]_ **Schema registry:** Unified schema registry to centrally discover and control event schemas with integration to major events platforms.

## Roadmap

LogUnify is under active development and there will be many new features to come. Please follow our [roadmap](https://www.logunify.com/#/roadmap) page for
features and timelines.

## Examples

Checkout the [examples](https://www.logunify.com/#/examples?id=example) page to see LogUnify in use.

## Community

Please join our [slack channel](https://join.slack.com/t/logunify/shared_invite/zt-1rko7zvlf-ENDYnVZGRb3v9FdoOBZp1A) to talk with our development team.
