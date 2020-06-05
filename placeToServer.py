import sys
import os
import shutil
from bs4 import BeautifulSoup

pom = BeautifulSoup(open('pom.xml'), features='xml')
build_file_prefix = pom.project.artifactId.text
spigot_dir = os.environ['SPIGOT_DIR']
target_dir = os.path.join(spigot_dir, 'plugins')
print("Build information - File name:", build_file_prefix)

if os.path.exists('./target'):
    file = \
        [i for i in os.listdir('./target') if
         os.path.isfile(os.path.join('./target', i)) and i.startswith(build_file_prefix)][0]
    file = os.path.join('./target', file)
    assert os.path.exists(file)

    oldfile = \
        [i for i in os.listdir(target_dir) if
         os.path.isfile(os.path.join(target_dir, i)) and i.startswith(build_file_prefix)]
    if len(oldfile) == 0:
        print("Destination directory has no previous program.")
    else:
        try:
            os.remove(os.path.join(target_dir, oldfile[0]))
        except Exception as e:
            print("Shut down your server first.")
            print(e)
            sys.exit(1)

    shutil.copy(file, target_dir)
    print("Deploy successful.")
    sys.exit(0)

print("Please build your project first.")
sys.exit(1)
