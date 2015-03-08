# RPM Package example

Structure of RPM package is defined in the openright rpm deploy package.  

It uses the Maven Tiles Plugin to mixin the structure of the RPM and RPM resources with the server artifact (here taken from the jaxws-jersey example)

It is possible to override the various RPM options through properties.
See example for some, the rpm deploy package for more.


# Installation on Redhat/Centos/Fedora

##using YUM with repository
`sudo yum install openright-rpm-package-example`
Assumes the artefact is available in a configured YUM repository.  
If using Nexus, it can be configured as a yum repository which easily integrates into build pipeline

## using YUM locally
`sudo yum install <rpm file name>`

# Installation on Ubuntu

## Using alien
`sudo alien -i --scripts <rpm file name>`
