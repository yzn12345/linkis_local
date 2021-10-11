import os, sys, getopt, traceback, json, re
import resource

# set memory
#memoryLimit = long(sys.argv[3])
#resource.setrlimit(resource.RLIMIT_AS,(memoryLimit,memoryLimit))

zipPaths = sys.argv[2]
paths = zipPaths.split(':')
for i in range(len(paths)):
  sys.path.insert(0, paths[i])

from py4j.java_gateway import java_import, JavaGateway, GatewayClient
from py4j.protocol import Py4JJavaError, Py4JNetworkError
import ast
import traceback
import warnings
import signal
import base64
import pandas as pd
from py4j.java_gateway import JavaGateway
from io import BytesIO
try:
  from StringIO import StringIO
except ImportError:
  from io import StringIO

# for back compatibility

class Logger(object):
  def __init__(self):
    pass

  def write(self, message):
    intp.appendOutput(message)

  def reset(self):
    pass

  def flush(self):
    pass


class PythonContext(object):
  """ A context impl that uses Py4j to communicate to JVM
  """

  def __init__(self, py):
    self.py = py
    self.max_result = 5000
    self._setup_matplotlib()

  def show(self, p, **kwargs):
    if hasattr(p, '__name__') and p.__name__ == "matplotlib.pyplot":
      self.show_matplotlib(p, **kwargs)
    elif type(p).__name__ == "DataFrame": # does not play well with sub-classes
      # `isinstance(p, DataFrame)` would req `import pandas.core.frame.DataFrame`
      # and so a dependency on pandas
      self.show_dataframe(p, **kwargs)
    elif hasattr(p, '__call__'):
      p() #error reporting

  def show_dataframe(self, df, show_index=False, **kwargs):
    """Pretty prints DF using Table Display System
    """
    limit = len(df) > self.max_result
    dt=df.dtypes
    dh=df.columns
    data = gateway.jvm.java.util.ArrayList()
    headers = gateway.jvm.java.util.ArrayList()
    schemas = gateway.jvm.java.util.ArrayList()

    for i in dh:
        headers.append(i)
       # print(dt[i])
        schemas.append(str(dt[i]))
    #for i in dt:
     #   schemas.append(i)
    for i in range(0,len(df)):
        iterms = gateway.jvm.java.util.ArrayList()
        for iterm in df.iloc[i]:
            iterms.append(str(iterm))
        data.append(iterms)
    intp.showDF(data,schemas,headers)

  def show_matplotlib(self, p, fmt="png", width="auto", height="auto",
                      **kwargs):
    """Matplotlib show function
    """
    if fmt == "png":
      img = BytesIO()
      p.savefig(img, format=fmt)
      img_str = b"data:image/png;base64,"
      img_str += base64.b64encode(img.getvalue().strip())
      img_tag = "<img src={img} style='width={width};height:{height}'>"
      # Decoding is necessary for Python 3 compability
      img_str = img_str.decode("utf-8")
      img_str = img_tag.format(img=img_str, width=width, height=height)
    elif fmt == "svg":
      img = StringIO()
      p.savefig(img, format=fmt)
      img_str = img.getvalue()
    else:
      raise ValueError("fmt must be 'png' or 'svg'")

    html = "<div style='width:{width};height:{height}'>{img}<div>"
    #print(html.format(width=width, height=height, img=img_str))
    intp.showHTML(html.format(width=width, height=height, img=img_str))
    img.close()

  def configure_mpl(self, **kwargs):
    import mpl_config
    mpl_config.configure(**kwargs)

  def _setup_matplotlib(self):
    # If we don't have matplotlib installed don't bother continuing
    try:
      import matplotlib
    except ImportError:
      return
    # Make sure custom backends are available in the PYTHONPATH
    rootdir = os.environ.get('ZEPPELIN_HOME', os.getcwd())
    mpl_path = os.path.join(rootdir, 'interpreter', 'lib', 'python')
    if mpl_path not in sys.path:
      sys.path.append(mpl_path)

    # Finally check if backend exists, and if so configure as appropriate
    try:
      matplotlib.use('module://backend_zinline')
      import backend_zinline

      # Everything looks good so make config assuming that we are using
      # an inline backend
      self.configure_mpl(width=600, height=400, dpi=72,
                         fontsize=10, interactive=True, format='png')
    except ImportError:
      # Fall back to Agg if no custom backend installed
      matplotlib.use('Agg')
      warnings.warn("Unable to load inline matplotlib backend, "
                    "falling back to Agg")


def handler_stop_signals(sig, frame):
  sys.exit("Got signal : " + str(sig))



def show_matplotlib(self, p, fmt="png", width="auto", height="auto",
                    **kwargs):
  """Matplotlib show function
  """
  if fmt == "png":
    img = BytesIO()
    p.savefig(img, format=fmt)
    img_str = b"data:image/png;base64,"
    img_str += base64.b64encode(img.getvalue().strip())
    img_tag = "<img src={img} style='width={width};height:{height}'>"
    # Decoding is necessary for Python 3 compability
    img_str = img_str.decode("utf-8")
    img_str = img_tag.format(img=img_str, width=width, height=height)
  elif fmt == "svg":
    img = StringIO()
    p.savefig(img, format=fmt)
    img_str = img.getvalue()
  else:
    raise ValueError("fmt must be 'png' or 'svg'")

  html = "<div style='width:{width};height:{height}'>{img}<div>"
  #print(html.format(width=width, height=height, img=img_str))
  intp.showHTML(html.format(width=width, height=height, img=img_str))
  img.close()

signal.signal(signal.SIGINT, handler_stop_signals)

_pUserQueryNameSpace = {}
client = GatewayClient(port=int(sys.argv[1]))

#gateway = JavaGateway(client, auto_convert = True)
gateway = JavaGateway(client)

output = Logger()
sys.stdout = output
sys.stderr = output
intp = gateway.entry_point

show = __show__ = PythonContext(intp)
__show__._setup_matplotlib()

intp.onPythonScriptInitialized(os.getpid())

while True :
  req = intp.getStatements()
  if req == None:
    break

  try:
    stmts = req.statements().split("\n")
    final_code = None
#     ori_code = req.statements()
#     if ori_code:
#         output.write(ori_code)

    for bdp_dwc_s in stmts:
      if bdp_dwc_s == None:
        continue

      # skip comment
      s_stripped = bdp_dwc_s.strip()
      if len(s_stripped) == 0 or s_stripped.startswith("#"):
        continue

      if final_code:
        final_code += "\n" + bdp_dwc_s
      else:
        final_code = bdp_dwc_s

    if final_code:
        compiledCode = compile(final_code, "<string>", "exec")
        eval(compiledCode)

    intp.setStatementsFinished("", False)
  except Py4JJavaError:
    excInnerError = traceback.format_exc() # format_tb() does not return the inner exception
    innerErrorStart = excInnerError.find("Py4JJavaError:")
    if innerErrorStart > -1:
       excInnerError = excInnerError[innerErrorStart:]
    intp.setStatementsFinished(excInnerError + str(sys.exc_info()), True)
  except Py4JNetworkError:
    # lost connection from gateway server. exit
    sys.exit(1)
  except:
    intp.setStatementsFinished(traceback.format_exc(), True)

  output.reset()