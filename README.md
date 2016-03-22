ORCID resolver and validator
============================

The project includes a Java implementation for an approach to resolve the ORCID iD of a user given the first name,
last name, and a list of DOIs for works claimed by the user at ORCID. The approach is reliable but does not
guarantee that the returned ORCID iD is correct.

The following lines demonstrate the usage of the resolver:

```java
OrcidResolver r = new OrcidResolver();

// id = 0000-0002-1900-4162
String id = r.resolve("Schindler", "Uwe", "10.1016/j.cageo.2008.02.023", "10.2481/dsj.5.79", "10.1007/11551362_12",
  "10.2312/wdc-mare.2005.1", "10.2312/wdc-mare.2005.3", "10.1016/S0098-3004(02)00039-0",
"10.1594/PANGAEA.854363", "10.1594/PANGAEA.760905", "10.1594/PANGAEA.760907"));
```
		
The project also includes an ORCID iD validator. It can be used as follows:

```java
// true
OrcidValidator.isValid("0000-0002-1900-4162")
```
