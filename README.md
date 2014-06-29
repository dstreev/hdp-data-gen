# Data Generation with MapReduce

## License
>   Licensed under the Apache License, Version 2.0 (the "License");
>   you may not use this file except in compliance with the License.
>   You may obtain a copy of the License at
>
>       http://www.apache.org/licenses/LICENSE-2.0
>
>   Unless required by applicable law or agreed to in writing, software
>   distributed under the License is distributed on an "AS IS" BASIS,
>   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
>   See the License for the specific language governing permissions and
>   limitations under the License.

## Summary
This is a Data Generator that will run in MapReduce to produce sample datasets that are configured by a json control file.  Output can be directed to HDFS or to a Kafka Topic.

## Building
A maven pom is provided and can be used to build the project with:
<code>mvn clean install</code>

This will also create a target/lib directory with the jar's that need to be copied to HDFS to support the Mappers.

## Running


## Json Schema and Examples
Are in the src/main/resources directory and will be a part of the binary created from maven.

## Supported Datatypes

### StringField
Supports both "Random" strings of a desired length range and "Sets".

#### Random String Configuration Example
<pre><code>
       {
            "string": {
                "random": {
                    "min": 10,
                    "max": 10,
                    "chars": "ABDEF12345",
                    "pool": {
                        "size": "100"
                    }
                }
            }
        }
</code></pre>

Random support a pool of string values when defined.  If present ("pool"), a random set of strings will be generated during construction based on the other settings.  These values are placed in the pool and these values are randomly given out during record creation.  This is helpful if you are trying to establish a field with a constrained bucket of values.  If the list is short, consider using "set" for more control.

#### Set String Configuration Example
<pre><code>
        {
            "string": {
                "set": ["ToMobile", "FromMobile"]
            }
        }
</code></pre>


### BooleanField

Produces a boolean output.  The "format" identifies the boolean values.  This can be anything. IE: true:false or F:T or YES:NO.  The values must be delimited by a ":" and will be randomly used during record creation.

<pre><code>
        {
            "boolean": {
                "format": "0:1"
            }
        }
</pre></code>

### DateField
Produces a random date between the two dates supplied in the range.  The range values must match the "format" element.  The "format" also controls the output format used during record creation.  The java "SimpleDateFormat" pattern values are used to define "format".

<pre><code>
        {
            "date": {
                "format": "yyyy-MM-dd HH:mm:ss",
                "range": {
                    "min": "2010-01-01 00:00:00",
                    "max": "2013-04-01 23:59:59"
                }
            }
        }
</pre></code>

### Start.Stop
Produces a start and stop date field that are consistent with each other and vary by a range determined in by the "spread" configuration.

The order, when supplied, is an array containing the precedence in the output.  If it isn't supplied the fields will be added (start then stop) to the record based on the order in the configuration file.

<pre><code>
        {
            "start.stop": {
                "order": [1,2],
                "format": "yyyy-MM-dd HH:mm:ss",
                "range": {
                    "min": "2010-01-01 00:00:00",
                    "max": "2013-04-01 23:59:59"
                },
                "spread": {
                    "min": 100,
                    "max": 100000
                }
            }
        }
</pre></code>

### IPAddressField
Produces a random Ip address between the values defined in "minIp" and "maxIp".  Pooling is also supported and follows the same pattern used for "pool" in the StringField.

<pre><code>
        {
            "ip": {
                "minIp": "192.168.14.0",
                "maxIp": "192.168.100.254",
                "pool": {
                    "size": 100
                }
            }
        }
</pre></code>

### ListField (not yet implemented)
The goal is to use a defined dictionary file of values for record creation.  This is helpful for generating data in different datasets that may share a common attribute used to later join them, like an account number.

### NumberField
Support the random creation of "int,long,float,double" values.  When non-real numbers are used, a "decimal" element controls the precision of the values used in record creation.

The produced numbers will be between the "min" and "max" values.

<pre><code>
       {
            "number": {
                "type": "long",
                "min": 4523100,
                "max": 9921000
            }
        },
        {
            "number": {
                "type": "float",
                "decimals": 2,
                "min": 10,
                "max": 1000
            }
        }
</pre></code>

### NullField
Create an empty field, useless the "nullvalue" element is set, in which case, that value will be used.

<pre><code>
        {
            "null": {
                "nullvalue": "NULL"
            }
        }
</pre></code>


## Field Positions
Field positions are determined by the order of the fields as defined in the json configuration file.  If you would like to override this, an "order" element can be added to the field definition.

If one field has an "order" element defined, then ALL fields must have an "order" element, otherwise the parser will complain.

The "order" doesn't need to be consecutive, just weighted.  Skipping values will NOT leave empty values, use the NullField for that.
<pre><code>
        {
            "string": {
                "order": 1,
                "set": ["ToMobile", "FromMobile"]
            }
        }
</pre></code>