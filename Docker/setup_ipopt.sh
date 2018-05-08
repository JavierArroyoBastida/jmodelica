# 
#    Copyright (C) 2018 Modelon AB
#
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the Common Public License as published by
#    IBM, version 1.0 of the License.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY. See the Common Public License for more details.
#
#    You should have received a copy of the Common Public License
#    along with this program.  If not, see
#     <http://www.ibm.com/developerworks/library/os-cpl.html/>.

IPOPT_VERSION=3.12.8

wget https://www.coin-or.org/download/source/Ipopt/Ipopt-${IPOPT_VERSION}.tgz

tar xvf Ipopt-${IPOPT_VERSION}.tgz
cd Ipopt-${IPOPT_VERSION}/ThirdParty/Blas
./get.Blas
cd ../Lapack
./get.Lapack
cd ../Mumps
./get.Mumps
cd ../Metis
./get.Metis
cd ../..

mkdir build
cd build
mkdir /build/ipopt-installation
../configure --prefix=/build/ipopt-installation
make install