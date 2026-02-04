{ pkgs ? import <nixpkgs> {} }:

let
  semeru25 = pkgs.stdenv.mkDerivation rec {
    pname = "ibm-semeru-25-local";
    version = "25";

    src = pkgs.fetchurl {
      url = "https://github.com/ibmruntimes/semeru25-binaries/releases/download/jdk-25.0.2%2B10_openj9-0.57.0/ibm-semeru-open-jdk_x64_linux_25.0.2_10_openj9-0.57.0.tar.gz";
      hash = "sha256-AsZxBuNeubwC/Xj1J9P/B7kDMyV5ru4nO9BWqYdI/Ws=";
    };

    nativeBuildInputs = [ pkgs.autoPatchelfHook ];

    buildInputs = with pkgs; [
      stdenv.cc.cc.lib
      zlib
      alsa-lib
      freetype
      xorg.libX11
      xorg.libXext
      xorg.libXrender
      xorg.libXi
      xorg.libXtst
    ];

    installPhase = ''
      mkdir -p $out
      cp -r * $out/
    '';
  };
  fixedAnt = pkgs.ant.overrideAttrs (oldAttrs: {
    installPhase = oldAttrs.installPhase + ''
      sed -i 's|lib/ant-launcher.jar|lib/ant-launcher.jar:$ANT_HOME/lib/ant.jar|' $out/bin/ant
    '';
  });
in
pkgs.mkShell {
  packages = [ semeru25 fixedAnt ];

  shellHook = ''
    export JAVA_HOME=${semeru25}
    unset CLASSPATH
    java -version
  '';
}
