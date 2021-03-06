ORCID Resolver and Validator
============================

The project includes a Java implementation for an approach to resolve the ORCID
iD of a user given the first name, last name, and a list of DOIs for works 
claimed by the user at ORCID. The approach is reliable but does not guarantee 
that the returned ORCID iD is correct.

The following lines demonstrate the usage of the resolver:

```java
OrcidResolver r = new OrcidResolver();

// id = 0000-0002-1900-4162
String id = r.resolve("Schindler", "Uwe",
  "10.1016/j.cageo.2008.02.023", "10.2481/dsj.5.79", "10.1007/11551362_12",
  "10.2312/wdc-mare.2005.1", "10.2312/wdc-mare.2005.3",
  "10.1016/S0098-3004(02)00039-0", "10.1594/PANGAEA.854363",
  "10.1594/PANGAEA.760905", "10.1594/PANGAEA.760907"));
```

The code is also able to handle names with abbreviated first name. Those are
detected if all letters of the first name are uppercase, e.g. "Schindler, UH"
(short for "Schindler, Uwe Horst"). The query to ORCID then uses wildcard
on the first name. The variants "U", "U H", "U.H.", "U. H.", and "U." are also 
supported.

The project also includes an ORCID iD validator that validates the checksum of 
the given ORCID. It can be used as follows:

```java
// true
OrcidValidator.isValid("0000-0002-1900-4162")
```
