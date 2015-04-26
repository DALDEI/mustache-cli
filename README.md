# mustache-cli
A CLI for mustache.java with enhanced JSON support 
Usage:
  mustache-cli [options] 

## Options
| Option | Arg | Description |
|--------|-----|--------|
| --root \|  -R \|  --template-dir| dirname |Template directory root|
| -f \|  --template-file| filename | Template file or '-'|
| -t \|  --template \|  --template-data | 'text' |Template data inline |
| -p \|  --properties-file |filename |Context read from Java properties file" 
| -j \|  --json-data |filename |Context  inline as JSON" 
| -J \|  --json-file | filename |Context  read from JSON file" 
| -o \|  --output \|  --output-file |filename |Write to output file or '-'" 
| -n \|  --name |filename |Template name|
| -S \|  --delim-start | "string" |Delmitar start string [default '{{']|
| -E \|  --delim-end dirname|Delmitar end string [default '}}']|
| --json ||Use JSON encoded data for variable expansion|
| --html ||Use HTML encoded data for variable expansion|
| -h \|  --help||Help|
