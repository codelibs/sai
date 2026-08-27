# Security Policy

## Supported versions

Only the latest release line receives security fixes.

| Version | Supported |
|---------|-----------|
| 0.3.x   | Yes       |
| < 0.3   | No        |

## Reporting a vulnerability

Please do not open a public issue for a security problem.

Report it through GitHub's private vulnerability reporting: go to the
[Security tab](https://github.com/codelibs/sai/security) of this repository and choose
**Report a vulnerability**. That opens a private advisory visible only to you and the
maintainers.

Please include:

- what an attacker can do, and what they need in order to do it
- a script or Java snippet that reproduces the behaviour
- the Sai version and the JDK you ran it on

You can expect an acknowledgement within a week. If a fix is warranted we will agree a
disclosure date with you and credit you in the advisory unless you prefer otherwise.

## Scope

Sai executes ECMAScript with full access to the Java platform. A script that reads
files, opens sockets, or loads classes is doing what the engine is designed to do - that
by itself is not a vulnerability.

What is in scope is the engine failing to hold a boundary it claims to hold:

- a script escaping a `ClassFilter` supplied through the JSR-223 API
- `--no-java` or the sandbox test constraints failing to prevent Java access
- a crafted script causing memory corruption, or code execution outside the engine's
  documented capabilities
- a denial of service that is disproportionate to the input, rather than a script that
  simply loops

If you are embedding Sai to run untrusted scripts, note that the JVM SecurityManager the
original Nashorn sandbox relied on is disabled by default from Java 18 onward. Isolate
untrusted scripts at the process or container level.
