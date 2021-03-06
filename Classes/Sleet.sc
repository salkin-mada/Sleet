Sleet {
	var <classpath, <modules, <synthdefs, <list;

	*new { | numChannels=2|
		^super.new.init( numChannels );
	}

	init { | numChannels |
		classpath = Main.packages.asDict.at('sleet');
		// I would rename the git repo to 'Sleet' so that this is the same as the Class.
		// hugs salkin
		modules = IdentityDictionary.new;
		list=IdentityDictionary.new;

		this.loadModulesToDict(numChannels);
		this.makeSynthDefs(numChannels);
		this.makeList();
	}

	loadModulesToDict{|numChannels|
		var folder = classpath +/+ "modules";

		// Iterate over a folder of files containing sleet modules
		PathName(folder).filesDo{|f|
			var ext = f.extension;
			var name = f.fileNameWithoutExtension.asSymbol;

			// Only normal SuperCollider files
			if(ext == "scd", {
				var contents;
				"found module file: %".format(name).poststamped;

				contents = f.fullPath.load.value(numChannels);

				modules.put(name, contents)
			})
		}

		^modules
	}

	get{|name|
		^list[name]
	}

	makeList{
		// Category
		modules.keysValuesDo{|category, catContent|
			// Modules in category
			catContent.keysValuesDo{|moduleName, moduleContent|
				list.put(moduleName, moduleContent)
			}
		};

		^list
	}

	storeSynths{
		synthdefs.do{|sd|
			sd.store
		}
	}

	makeSynthDefs{|numChannels=2|

		// Category
		modules.keysValuesDo{|category, catContent|

			// Modules in category
			catContent.keysValuesDo{|moduleName, moduleContent|
				var def, defname;
				defname = moduleName.asString ++ numChannels;

				"loading module % from category % ".format(moduleName, category).poststamped;

				// Create synthdef
				def = SynthDef(defname.asSymbol, { |in, out, wet=1.0|
					var insig = In.ar(in, numChannels);
					var sig = SynthDef.wrap(moduleContent, prependArgs: [insig]);

					XOut.ar(out, wet, sig);
				}).add;

				// Add to global synthdef array of instance
				synthdefs = synthdefs.add(def);

				"SynthDef added: SynthDef('%')".format(defname).poststamped;

			}
		}

	}
}
