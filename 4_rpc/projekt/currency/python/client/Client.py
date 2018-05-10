import sys
import glob

# sys.path.append('generated')
# sys.path.insert(0, glob.glob('../../lib/py/build/lib*')[0])

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.protocol import TMultiplexedProtocol
from  generated.bank.ds.agh import AccountService
from  generated.bank.ds.agh import AccountManagement
from  generated.bank.ds.agh import PremiumAccountService
from generated.bank.ds.agh.ttypes import *

currencies = ["PLN", "USD", "EUR", "CHF", "GBP"]


def initThrift(host, port):
    transport = TSocket.TSocket(host, port)
    transport = TTransport.TBufferedTransport(transport)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)
    aM = AccountManagement.Client(TMultiplexedProtocol.TMultiplexedProtocol(protocol, "manager"))
    aS = AccountService.Client(TMultiplexedProtocol.TMultiplexedProtocol(protocol, "standard"))
    paS = PremiumAccountService.Client(TMultiplexedProtocol.TMultiplexedProtocol(protocol, "premium"))
    transport.open()
    return transport, aM, aS, paS


if __name__ == '__main__':
    port = int(input("Enter your bank port number"))
    trans, AM, AS, PAS = initThrift("0.0.0.0", port)
    run_flag = True
    while (run_flag):
        cmd = input("Enter command : create, info, credit, exit")
        if cmd == 'exit':
            run_flag = False
            trans.close()
        elif cmd == "create":
            acc_info = input("Enter : \"pesel;firstname;lastname;income;baseCurrency\"")
            p, f, l, i, b = acc_info.split(";")
            i = float(i)
            acc = Account(p, f, l, i, b)
            response = AM.createAccount(acc)
            print(response)
        elif cmd == "info":
            p = input("Enter your pesel")
            response = AS.getAccountDetails(p)
            print(response)
        elif cmd == "credit":
            p = input("Enter your pesel")
            cur, c, start, stop = input("Enter credit parrameters : \"currency;cost;startdate(year-month);enddate\"")
            c = float(c)
            start = start.split("-")
            stop = stop.split("-")
            start = ThriftDate(int(start[0]), int(start[1]))
            stop = ThriftDate(int(stop[0]), int(stop[1]))
            credit_param = CreditParameters(cur, c, start, stop)
            response = PAS.getCreditCosts(p, credit_param)
            print(response)
        else:
            print("Incorrect command")
